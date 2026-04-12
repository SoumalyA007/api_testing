    const jsonServer = require("json-server");
    const jwt = require("jsonwebtoken");
    const fs = require("fs");

    const server = jsonServer.create();
    const router = jsonServer.router("db.json");
    const middlewares = jsonServer.defaults();
    const SECRET = "my_super_secret_key_which_is_long_enough_12345";
    const ORDER_STATUSES = ["PENDING", "PROCESSING", "COMPLETE", "REFUNDED"];

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

//      if (!["admin", "user"].includes(role)) {
//        return res.status(400).json({ error: "Role must be admin or user" });
//      }

      // ✅ Allow missing or 0 ID (server will generate)
      if (id !== undefined && id !== 0) {
        if (!Number.isInteger(id) || id <= 0) {
          return res.status(400).json({ error: "ID must be positive" });
        }
      }

      next();
    }

    function validateRole(req, res, next) {
      const { role } = req.body;

      if (role && !["admin", "user"].includes(role)) {
        return res.status(400).json({
          error: "Role must be admin or user"
        });
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

    function validateProduct(req, res, next) {

      const { title, price, category, description, image } = req.body;

      // Title validation
      if (!title || typeof title !== "string" || title.trim() === "") {
        return res.status(400).json({
          error: "Product title is required"
        });
      }

      // Price validation
        if (!Number.isFinite(price) || price <= 0) {
          return res.status(400).json({
            error: "Price must be a positive number"
          });
        }

      // Category validation
      if (!category || typeof category !== "string") {
        return res.status(400).json({
          error: "Category is required"
        });
      }

      // Description validation
      if (!description || typeof description !== "string") {
        return res.status(400).json({
          error: "Description is required"
        });
      }

      // Image validation
      if (!image || typeof image !== "string") {
        return res.status(400).json({
          error: "Image URL is required"
        });
      }



      const urlRegex = /^https?:\/\/.+/;

      if (!urlRegex.test(image)) {
        return res.status(400).json({
          error: "Invalid image URL"
        });
      }

      if (description.includes("<script>")) {
        return res.status(400).json({
          error: "Description contains invalid content"
        });
      }

      next();
    }

    // ── Permission middleware ────────────────────────────────────────────────────
    server.use((req, res, next) => {
      // Extract the top-level resource name from the path
      const resource = req.path.split("/").filter(Boolean)[0];
      const perm = PERMISSIONS[resource];

      // Unknown resource — let json-server handle it (will 404)
      if (!perm) return next();

      const role  = getCallerRole(req);

      if (role === "public") {
         return res.status(401).json({ error: "Authentication required" });
      }
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
      // 🔹 Ownership enforcement for USER role
      if (role === "user" && ["orders", "carts"].includes(resource)) {

        // GET → restrict query
        if (req.method === "GET") {
          req.query.userId = String(req.user.id);
        }

        const db = router.db.getState();
        const collection = db[resource];

        // CREATE
        if (req.method === "POST") {
          if (req.body.userId && req.body.userId !== req.user.id) {
            return res.status(403).json({
              error: "Cannot create resource for another user"
            });
          }

          // Force ownership
          req.body.userId = req.user.id;
        }

        // UPDATE / DELETE
        if (["GET", "PUT", "PATCH", "DELETE"].includes(req.method) && req.path.split("/")[2]) {

          //const id = req.params.id;   // ✅ use this instead of split
          const id = req.path.split("/")[2];
          const existingItem = collection.find(item => item.id == id);

          if (!existingItem) {
            return res.status(404).json({ error: "Resource not found" });
          }

          if (existingItem.userId !== req.user.id) {
            return res.status(403).json({
              error: "You cannot modify another user's resource"
            });
          }

          // 🔥 Prevent ownership tampering
          if (req.body) {
            req.body.userId = existingItem.userId;
          }
        }
      }

        // 🔹 Validate productId exists for carts
        if (resource === "carts" && ["POST", "PUT", "PATCH"].includes(req.method)) {

          const db = router.db.getState();
          const allUsers = db.users;
          const allProducts = db.products;

          // 🔹 Validate userId exists (ADMIN only)
          if (role === "admin") {
            const userExists = allUsers.some(u => u.id === req.body.userId);

            if (!userExists) {
              return res.status(400).json({
                error: `User with id ${req.body.userId} does not exist`
              });
            }
          }

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

          const mergedProducts = {};

            for (const item of req.body.products) {
              if (mergedProducts[item.productId]) {
                mergedProducts[item.productId] += item.quantity;
              } else {
                mergedProducts[item.productId] = item.quantity;
              }
            }

            req.body.products = Object.entries(mergedProducts).map(
              ([productId, quantity]) => ({
                productId: Number(productId),
                quantity
              })
            );


            // 🔹 Enforce ONE CART per user (POST only)
            if (req.method === "POST") {

              const existingCart = db.carts.find(
                cart => cart.userId === req.body.userId
              );

              if (existingCart) {

                // Merge with existing cart instead of creating new one
                const combinedProducts = {};

                // Existing products
                for (const item of existingCart.products) {
                  combinedProducts[item.productId] = item.quantity;
                }

                // New products
                for (const item of req.body.products) {
                  if (combinedProducts[item.productId]) {
                    combinedProducts[item.productId] += item.quantity;
                  } else {
                    combinedProducts[item.productId] = item.quantity;
                  }
                }

                existingCart.products = Object.entries(combinedProducts).map(
                  ([productId, quantity]) => ({
                    productId: Number(productId),
                    quantity
                  })
                );

                router.db.write();

                return res.status(200).json(existingCart);
              }
            }
        }

      next();
    });

    // ── Secure user creation ─────────────────────────────────────────────
    server.post("/users", (req, res, next) => {

      const role = getCallerRole(req);

      // 🔒 Block public
      if (role === "public") {
        return res.status(401).json({ error: "Authentication required" });
      }

      // ─────────────────────────────
      // ✅ 1. DEFAULT ROLE FIRST
      // ─────────────────────────────
      if (!req.body.role) {
        req.body.role = "user";
      }

      // ─────────────────────────────
      // ✅ 2. ROLE AUTHORIZATION
      // ─────────────────────────────
      if (req.body.role === "admin" && role !== "admin") {
        return res.status(403).json({
          error: "Only admin can assign admin role"
        });
      }

      // ─────────────────────────────
      // ✅ 3. NOW VALIDATE
      // ─────────────────────────────
      validateUser(req, res, function (err) {
        if (err) return next(err);

        validateRole(req, res, function (err) {
          if (err) return next(err);

          // 🔥 ADD THIS BLOCK (duplicate email check)
          const db = router.db;
          const users = db.get("users").value();

          const emailExists = users.some(
            u => u.email && req.body.email &&
                 u.email.toLowerCase() === req.body.email.toLowerCase()
          );

          if (emailExists) {
            return res.status(409).json({
              error: "Email already exists"
            });
          }

          next(); // continue to json-server
        });
      });

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

      const { email, username, password } = req.body;

      if (!email || !username || !password) {
        return res.status(400).json({
          error: "Email, username and password are required"
        });
      }

      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

      if (!emailRegex.test(email)) {
        return res.status(400).json({
          error: "Invalid email format"
        });
      }

      if (password.length < 6) {
        return res.status(400).json({
          error: "Password must be at least 6 characters"
        });
      }

      const db = router.db;

      const existingUserByEmail = db.get("users")
        .find({ email })
        .value();

      if (existingUserByEmail) {
        return res.status(409).json({
          error: "Email already registered"
        });
      }

      const existingUserByUsername = db.get("users")
        .find({ username })
        .value();

      if (existingUserByUsername) {
        return res.status(409).json({
          error: "Username already taken"
        });
      }

      const newUser = {
        id: Date.now(),
        email,
        username,
        password,
        role: "user"
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

    // ── Order update/delete rules ─────────────────────────────────────────
    server.use("/orders", (req, res, next) => {

      const role = getCallerRole(req);

      if (role === "public") {
        return res.status(401).json({ error: "Authentication required" });
      }

      if (role === "invalid") {
        return res.status(401).json({ error: "Invalid or expired token" });
      }

      const db = router.db.getState();
      const orders = db.orders;

      const orderId = req.path.split("/")[1];
      const order = orders.find(o => o.id == orderId);

      // UPDATE ORDER
      if (["PUT", "PATCH"].includes(req.method)) {

        // ❌ Users cannot update orders
        if (role === "user") {
          return res.status(403).json({
            error: "Users cannot update orders"
          });
        }

        // ✅ Admin update rules
        if (role === "admin") {

          // Only status update allowed
          if (!req.body.status) {
            return res.status(400).json({
              error: "Admin can only update order status"
            });
          }

          if (!ORDER_STATUSES.includes(req.body.status)) {
            return res.status(400).json({
              error: `Invalid status. Allowed: ${ORDER_STATUSES.join(", ")}`
            });
          }

          // Remove all other fields
          req.body = { status: req.body.status };
        }
      }

      // DELETE ORDER
      if (req.method === "DELETE") {

        if (!order) {
          return res.status(404).json({
            error: "Order not found"
          });
        }

        // User can delete only their order
        if (role === "user" && order.userId !== req.user.id) {
          return res.status(403).json({
            error: "You cannot delete another user's order"
          });
        }
      }

      next();
    });

    // ── Order Creation Business Logic ─────────────────────────────────────
//    server.post("/orders", (req, res, next) => {
//
//      const db = router.db;
//
//      const { userId, items } = req.body;
//
//      if (!items || !Array.isArray(items) || items.length === 0) {
//        return res.status(400).json({ error: "Products array required" });
//      }
//
//      const allProducts = db.get("products").value();
//      const inventory = db.get("inventory");
//
//      let totalPrice = 0;
//
//      for (const item of items) {
//
//        if (!Number.isInteger(item.quantity) || item.quantity <= 0) {
//            return res.status(400).json({
//              error: `Invalid quantity for product ${item.productId}`
//            });
//          }
//
//        const product = allProducts.find(p => p.id === item.productId);
//
//        if (!product) {
//          return res.status(400).json({
//            error: `Product ${item.productId} not found`
//          });
//        }
//
//        const stockItem = inventory
//          .find({ productId: item.productId })
//          .value();
//
//        if (!stockItem || stockItem.quantity < item.quantity) {
//          return res.status(400).json({
//            error: `Insufficient inventory for product ${item.productId}`
//          });
//        }
//
//        totalPrice += product.price * item.quantity;
//      }
//
//      // Reduce inventory
//      for (const item of items) {
//
//        const stockItem = inventory
//          .find({ productId: item.productId })
//          .value();
//
//        inventory
//          .find({ productId: item.productId })
//          .assign({
//            quantity: stockItem.quantity - item.quantity
//          })
//          .write();
//      }
//
//      // Clear cart
//      db.get("carts")
//        .remove({ userId: userId })
//        .write();
//
//      // Add calculated fields
//      req.body.totalPrice = totalPrice;
//      req.body.status = "PENDING";
//      req.body.createdAt = new Date().toISOString();
//
//      next();
//    });


server.post("/orders", (req, res, next) => {

  const db = router.db;

  const { userId, items } = req.body;

  const allProducts = db.get("products").value();
  const allUsers = db.get("users").value();
  const inventory = db.get("inventory");

  // ─────────────────────────────────────────────
  // ✅ 1. REQUEST STRUCTURE VALIDATION
  // ─────────────────────────────────────────────

  if (!Number.isInteger(userId) || userId <= 0) {
    return res.status(400).json({
      error: "Valid userId is required"
    });
  }

  if (!items || !Array.isArray(items) || items.length === 0) {
    return res.status(400).json({
      error: "Items array is required"
    });
  }

  // ─────────────────────────────────────────────
  // ✅ 2. USER VALIDATION
  // ─────────────────────────────────────────────

  const userExists = allUsers.some(u => u.id === userId);

  if (!userExists) {
    return res.status(400).json({
      error: `User ${userId} does not exist`
    });
  }

  // ─────────────────────────────────────────────
  // ✅ 3. ITEMS VALIDATION
  // ─────────────────────────────────────────────

  let totalPrice = 0;

  for (const item of items) {

    // 🔥 productId validation
    if (!Number.isInteger(item.productId)) {
      return res.status(400).json({
        error: "Invalid productId"
      });
    }

    // 🔥 quantity validation (MOST IMPORTANT)
    if (!Number.isInteger(item.quantity) || item.quantity <= 0) {
      return res.status(400).json({
        error: `Invalid quantity for product ${item.productId}`
      });
    }

    // 🔥 product existence
    const product = allProducts.find(p => p.id === item.productId);

    if (!product) {
      return res.status(400).json({
        error: `Product ${item.productId} not found`
      });
    }

    // 🔥 inventory existence
    const stockItem = inventory
      .find({ productId: item.productId })
      .value();

    if (!stockItem) {
      return res.status(400).json({
        error: `Inventory not found for product ${item.productId}`
      });
    }

    // 🔥 stock check
    if (stockItem.quantity < item.quantity) {
      return res.status(400).json({
        error: `Insufficient inventory for product ${item.productId}`
      });
    }

    totalPrice += product.price * item.quantity;
  }

  // ─────────────────────────────────────────────
  // ✅ 4. BUSINESS LOGIC (ONLY AFTER VALIDATION)
  // ─────────────────────────────────────────────

  for (const item of items) {

    const stockItem = inventory
      .find({ productId: item.productId })
      .value();

    inventory
      .find({ productId: item.productId })
      .assign({
        quantity: stockItem.quantity - item.quantity
      })
      .write();
  }

  // Clear cart
  db.get("carts")
    .remove({ userId: userId })
    .write();

  // Final order fields
  req.body.totalPrice = totalPrice;
  req.body.status = "PENDING";
  req.body.createdAt = new Date().toISOString();

  next();
});

    server.use("/products", (req, res, next) => {

      if (["POST", "PUT", "PATCH"].includes(req.method)) {
        return validateProduct(req, res, next);
      }

      next();
    });

server.use("/inventory", (req, res, next) => {

  if (["POST", "PUT", "PATCH"].includes(req.method)) {

    const db = router.db;
    const products = db.get("products").value();
    const inventory = db.get("inventory").value();

    const { productId, quantity, minThreshold, warehouse } = req.body;

    // ── ✅ Validate product exists ─────────────────────────────
    if (productId !== undefined) {
      const productExists = products.some(p => p.id === productId);

      if (!productExists) {
        return res.status(400).json({
          error: `Product ${productId} does not exist`
        });
      }
    }

    // ── ✅ Quantity validation ─────────────────────────────
    if (quantity !== undefined) {
      if (!Number.isInteger(quantity) || quantity < 0) {
        return res.status(400).json({
          error: "Quantity must be a non-negative integer"
        });
      }
    }

    // ── ✅ minThreshold validation ─────────────────────────────
    if (minThreshold !== undefined) {
      if (!Number.isInteger(minThreshold) || minThreshold < 0) {
        return res.status(400).json({
          error: "minThreshold must be a non-negative integer"
        });
      }
    }

    // ── ✅ Warehouse validation ─────────────────────────────
    if (warehouse !== undefined) {
      if (!warehouse || typeof warehouse !== "string") {
        return res.status(400).json({
          error: "Warehouse must be a valid string"
        });
      }
    }

    // ── 🔥 Duplicate check (productId must be UNIQUE) ─────────
    if (productId !== undefined) {

      // Extract ID for PUT/PATCH (if exists)
      const currentId = req.path.split("/")[1];

      const duplicate = inventory.find(item =>
        item.productId === productId &&
        item.id != currentId // ignore same record during update
      );

      if (duplicate) {
        return res.status(409).json({
          error: `Inventory already exists for productId ${productId}`
        });
      }
    }
  }

  next();
});

server.delete("/products/:id", (req, res, next) => {

  const db = router.db;
  const productId = Number(req.params.id);

  // ✅ Delete related inventory first
  db.get("inventory")
    .remove({ productId })
    .write();

  next(); // then delete product
});

//    server.post("/products", (req, res, next) => {
//
//      const db = router.db;
//
//      const { id } = req.body;
//
//      // Let product be created first
//      next();
//
//      // After creation → create empty inventory
//      setTimeout(() => {
//
//        const inventoryExists = db.get("inventory")
//          .find({ productId: id })
//          .value();
//
//        if (!inventoryExists) {
//          db.get("inventory")
//            .push({
//              id: Date.now(),
//              productId: id,
//              warehouse:"virtual"
//              quantity: 99,
//              minThreshold: 1
//            })
//            .write();
//        }
//
//      }, 0);
//    });

      server.post("/products", (req, res, next) => {

        res.on("finish", () => {
          const db = router.db;

          const latestProduct = db.get("products").value().slice(-1)[0];

          db.get("inventory")
            .push({
              id: Date.now(),
              productId: latestProduct.id,
              quantity: 99,
              warehouse: "Default",
              minThreshold: 1
            })
            .write();
        });

        next();
      });

      server.use("/categories", (req, res, next) => {

        if (req.method === "POST") {

          const db = router.db;
          const categories = db.get("categories").value();

          const { name } = req.body;

          if (!name || typeof name !== "string") {
            return res.status(400).json({
              error: "Category name is required"
            });
          }

          const exists = categories.some(
            c => c.name.toLowerCase() === name.toLowerCase()
          );

          if (exists) {
            return res.status(409).json({
              error: "Category already exists"
            });
          }
        }

        next();
      });

      server.use("/inventory", (req, res, next) => {
        const allowedParams = ["productId", "warehouse", "quantity", "minThreshold"];

        const queryKeys = Object.keys(req.query);

        const invalidKeys = queryKeys.filter(k => !allowedParams.includes(k));

        if (invalidKeys.length > 0) {
          return res.status(400).json({
            error: `Invalid query params: ${invalidKeys.join(", ")}`
          });
        }

        next();


      });





    // ── Mount router ─────────────────────────────────────────────────────────────
    server.use(router);

    server.listen(3000, () => {
      console.log("JSON Server with auth running on http://localhost:3000");
    });