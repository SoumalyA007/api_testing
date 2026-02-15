package utilities;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ExtentReportManager implements ITestListener {
     public ExtentSparkReporter sparkReporter;
     public ExtentReports extent;
     public ExtentTest test;
     String reportName;

     public void onStart(ITestContext context){

         String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
         reportName = "Test-Api-Report"+timeStamp+".html";

         sparkReporter = new ExtentSparkReporter(".\\reports\\" + reportName);
         sparkReporter.config().setDocumentTitle("RestAssuredAutomationProject"); // Title of report
         sparkReporter.config().setReportName("Pet Store Users API"); // name of the report
         sparkReporter.config().setTheme(Theme.DARK);

         extent = new ExtentReports();
         extent.attachReporter(sparkReporter);

         extent.setSystemInfo("Application", "Fakestore API ");
         extent.setSystemInfo("Operating System", System.getProperty("os.name"));
         extent.setSystemInfo("User Name", System.getProperty("user.name"));
         extent.setSystemInfo("Environemnt","QA");
         extent.setSystemInfo("user","Soumalya");

     }

     public void onTestSuccess(ITestResult results){
         test = extent.createTest(results.getName());
         test.assignCategory(results.getMethod().getGroups());
         test.createNode(results.getName());
         test.log(Status.PASS,"Passed Successfully");
     }

     public void onTestFailure(ITestResult result){
         test = extent.createTest(result.getName());
         test.assignCategory(result.getMethod().getGroups());
         test.createNode(result.getName());
         test.log(Status.FAIL," Test Failed!!!!");
         test.log(Status.FAIL,result.getThrowable().getMessage());
     }

     public void onTestSkip(ITestResult result){
         test = extent.createTest(result.getName());
         test.assignCategory(result.getMethod().getGroups());
         test.createNode(result.getName());
         test.log(Status.SKIP,"Skipped?????????");
         test.log(Status.SKIP,result.getThrowable().getMessage());
     }

     public void onFinish(ITestContext context){
         extent.flush();
     }



}
