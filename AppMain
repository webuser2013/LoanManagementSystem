package Selenium.maven.Selenium.maven.auto;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

public class AppMain {
	
 	
	public static void main(String[] args) {
 		try {
 			WebDriver driver;
 			DesiredCapabilities caps = DesiredCapabilities.internetExplorer();
 			caps.setCapability(InternetExplorerDriver.INITIAL_BROWSER_URL, "https://www.google.com/");
 			System.setProperty ("webdriver.ie.driver","./IEDriverServer32.exe" );
 			driver = (WebDriver) new InternetExplorerDriver(caps); 
 			System.out.println("implicitlyWait sleeping....");
 			driver.manage().timeouts().implicitlyWait(10,TimeUnit.SECONDS);
 			
 			Thread.sleep(10000);
   			
			driver.get("https://www.google.com/");
  			driver.manage().window().maximize();
  			
  			driver.findElement(By.xpath("//*[@id='tsf']/div[2]/div[1]/div[1]/div/div[2]/input")).sendKeys("search text....");
 			 
		} catch (Exception e) {
			e.printStackTrace();
		}
 	}
}
