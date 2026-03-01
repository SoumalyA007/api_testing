const jsonServer = require("json-server");
const jwt = require("jsonwebtoken");
const fs = require("fs");

const server = jsonServer.create();
const router = jsonServer.router("db.json");
const middlewares = jsonServer.defaults();
const SECRET = "my_super_secret_key_which_is_long_enough_12345";

// ── Unix-style permission table ──────────────────────────────────────────────
// Each resource: [ownerRead, ownerWrite, groupRead, groupWrite, otherRead, otherWrite]
// Mapped from octal: 6=rw, 4=r, 0=none
const PERMISSIONS = {
  users:     { admin: "rw", user: "r",   public: ""  },
  orders:    { admin: "rw", user: "rw", public: ""  },
  carts:     { admin: "rw", user: "rw", public: ""  },
  inventory: { admin: "rw", user: "",  public: "" },
  shipping:  { admin: "rw", user: "r", public: ""  },
  products:  { admin: "rw", user: "r", public: "r" },
};

// ── Route rewrites ───────────────────────────────────────────────────────────
server.use(jsonServer.rewriter({
  "/api/v1/*":             "/$1",
  "/auth/login":           "/login",
  "/products/categories":  "/categories",
  "/products/category/:name": "/products?category=:name",
  "/my-orders":            "/orders?userId=:userId",
  "/track/:id":            "/shipping?orderId=:id",
}));

server.use(middlewares);
server.use(jsonServer.bodyParser);

// ── Auth helper ──────────────────────────────────────────────────────────────
function getCallerRole(req) {
  const auth = req.headers.authorization || "";
  const token = auth.startsWith("Bearer ") ? auth.slice(7) : null;

  if (!token) return "public";

  try {
    const decoded = jwt.verify(token, SECRET);
    req.user = decoded;   // attach decoded user
    return decoded.role === "admin" ? "admin" : "user";
  } catch (err) {
    req.authError = err;
    return "invalid";
  }
}

function validateUser(req, res, next) {
  const { email, password, role, id } = req.body;

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  if (!emailRegex.test(email)) {
    return res.status(400).json({ error: "Invalid email format" });
  }

  if (!password || password.length < 6) {
    return res.status(400).json({ error: "Password must be at least 6 characters" });
  }

  if (!["admin", "user"].includes(role)) {
    return res.status(400).json({ error: "Role must be admin or user" });
  }

  if (!Number.isInteger(id) || id <= 0) {
    return res.status(400).json({ error: "ID must be positive" });
  }

  next();
}

// ── Login endpoint ───────────────────────────────────────────────────────────
server.post("/login", (req, res) => {

   if (!req.is("application/json")) {
    return res.status(415).json({ error: "Unsupported Media Type" });
  }
  const { username, password } = req.body;

  if (!username || !password) {
      return res.status(400).json({ error: "Username and password required" });
  }

  const db = router.db.getState();
  const user = db.users.find(
    (u) => u.username === username && u.password === password
  );
  if (!user) return res.status(401).json({ error: "Invalid credentials" });

  const token = jwt.sign(
    { id: user.id, username: user.username, role: user.role },
    SECRET,
    { expiresIn: "8h" }
  );
  res.json({ token, role: user.role, userId: user.id });
});

// ── Permission middleware ────────────────────────────────────────────────────
server.use((req, res, next) => {
  // Extract the top-level resource name from the path
  const resource = req.path.split("/").filter(Boolean)[0];
  const perm = PERMISSIONS[resource];

  // Unknown resource — let json-server handle it (will 404)
  if (!perm) return next();

  const role  = getCallerRole(req);
  if (role === "invalid") {
        return res.status(401).json({ error: "Invalid or expired token" });
    }
  const allowed = perm[role] || "";
  const isWrite = ["POST", "PUT", "PATCH", "DELETE"].includes(req.method);
  const isRead  = req.method === "GET";

  if (isRead  && !allowed.includes("r"))
    return res.status(403).json({ error: "Read access denied" });
  if (isWrite && !allowed.includes("w"))
    return res.status(403).json({ error: "Write access denied" });


  // For 'user' role: scope /orders and /carts to their own userId only
  // Enforce ownership for USER role
  if (role === "user" && ["orders", "carts"].includes(resource)) {

    // GET → restrict query
    if (req.method === "GET") {
      req.query.userId = String(req.user.id);
    }

    // POST → block creating resource for another user
    if (req.method === "POST") {
      if (req.body.userId && req.body.userId !== req.user.id) {
        return res.status(403).json({
          error: "Cannot create resource for another user"
        });
      }
    }

    // PUT / PATCH → block modifying another user’s resource
    if (["PUT", "PATCH"].includes(req.method)) {
      if (req.body.userId && req.body.userId !== req.user.id) {
        return res.status(403).json({
          error: "Cannot modify another user's resource"
        });
      }
    }
  }

    // 🔹 Validate productId exists for carts
    if (resource === "carts" && ["POST", "PUT", "PATCH"].includes(req.method)) {

      const db = router.db.getState();
      const allProducts = db.products;

      if (!req.body.products || !Array.isArray(req.body.products)) {
        return res.status(400).json({ error: "Products array is required" });
      }

      for (const item of req.body.products) {

        const productExists = allProducts.some(p => p.id === item.productId);

        if (!productExists) {
          return res.status(400).json({
            error: `Product with id ${item.productId} does not exist`
          });
        }

        if (!Number.isInteger(item.quantity) || item.quantity <= 0) {
          return res.status(400).json({
            error: "Quantity must be positive integer"
          });
        }
      }
    }

  next();
});

// ── Secure user creation ─────────────────────────────────────────────
server.post("/users", (req, res, next) => {
  const role = getCallerRole(req);

  // Block public
  if (role === "public") {
    return res.status(401).json({ error: "Authentication required" });
  }

  // Only admin can assign admin role
  if (role !== "admin") {
    req.body.role = "user";
  }

  // Default role
  if (!req.body.role) {
    req.body.role = "user";
  }

  next();
});

// ── Prevent role updates ─────────────────────────────────────────────
server.use((req, res, next) => {
  if (
    req.method === "PATCH" ||
    req.method === "PUT"
  ) {
    if (req.path.startsWith("/users")) {
      const role = getCallerRole(req);

      // Only admin can change roles
      if (req.body.role && role !== "admin") {
        delete req.body.role;
      }
    }
  }
  next();
});



// ── Register endpoint ─────────────────────────────────────────────
server.post("/auth/register", (req, res) => {

  if (!req.is("application/json")) {
    return res.status(415).json({ error: "Unsupported Media Type" });
  }

  const { username, password } = req.body;

  if (!username || !password) {
    return res.status(400).json({ error: "Username and password required" });
  }

  const db = router.db;
  const existingUser = db.get("users")
    .find({ email })
    .value();

  if (existingUser) {
    return res.status(409).json({ error: "User already exists" });
  }

  const newUser = {
    id: Date.now(),
    username,
    password,
    role: "user"   // 🔥 FORCE role here
  };

  db.get("users")
    .push(newUser)
    .write();

  res.status(201).json({
    message: "User registered successfully"
  });
});

server.post("/auth/logout", (req, res) => {
  const auth = req.headers.authorization || "";
  const token = auth.startsWith("Bearer ") ? auth.slice(7) : null;

  if (!token) {
    return res.status(400).json({ error: "No token provided" });
  }

  tokenBlacklist.add(token);

  res.json({ message: "Logged out successfully" });
});




// ── Mount router ─────────────────────────────────────────────────────────────
server.use(router);

server.listen(3000, () => {
  console.log("JSON Server with auth running on http://localhost:3000");
});