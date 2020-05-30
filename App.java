package Selenium.maven.Selenium.maven.auto;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.ie.InternetExplorerDriver;

public class App {
	public static void main(String[] args) {
		WebDriver driver = null;
		try {
			System.out.println("Hello World!");
			
			//C:/eBBS/Project/Explore/IEDriverServer_Win32_3.14.0/IEDriverServer32.exe
			//C:/eBBS/Project/Explore/IEDriverServer_x64_3.150.1/IEDriverServer.exe
 			//C:/eBBS/Project/Explore/ArunR2/IEDriverServer32.exe
			System.setProperty ("webdriver.ie.driver","C:/eBBS/Project/Explore/ArunR2/IEDriverServer32.exe" );
  			driver = (WebDriver) new InternetExplorerDriver(); 
  			
			//driver.get("https://www.truecaller.com/auth/sign-in");
			driver.get("https://www.google.com/");
			driver.manage().window().maximize();
 			
 			System.out.println("Before Wait.....");
  			System.out.println(">>>"+driver.getCurrentUrl());
			//driver.findElement(By.xpath("//*[@id='app']/main/div/div[1]")).click();
  			
    		System.out.println("Title:"+driver.getTitle());
  			
  			WebElement elem = driver.findElement(By.name("q"));//finding the web element using name locator
  			elem.sendKeys(new String[]{"selenium is fun"});
  			elem.submit();
 			
 			//driver.findElement(By.xpath("//*[@id='tsf']/div[2]/div[1]/div[1]/div/div[2]/input")).sendKeys("search Txt....");
		} catch (Exception e) {
			System.err.println("error..............");
 			e.printStackTrace();
 			driver.close();
		}
 		 
	}
}
