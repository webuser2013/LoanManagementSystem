package tamilrockers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class TamilRockersSelenium 
{
	public static void main(String[] args)
	{
		System.setProperty("webdriver.chrome.driver","C:\\Users\\muthu-5992\\Downloads\\chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		
		String baseUrl = "https://tamilrockers.unblockit.me/";
		String forumsUrl = "";
		String newTamilMoviesUrl = "";
		driver.get(baseUrl);
		
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<WebElement> listOfElements = driver.findElements(By.xpath("//a"));
		for (int i=0; i<listOfElements.size();i++)
		{
			if("Go to Forums".equalsIgnoreCase(listOfElements.get(i).getAttribute("title").toString()))
			{
				forumsUrl = listOfElements.get(i).getAttribute("href");
				System.out.println(forumsUrl);
				break;
			}
		}
		if(forumsUrl != null && !forumsUrl.isEmpty())
		{
			driver.get(forumsUrl);
			
			List<WebElement> listOfSubElements = driver.findElements(By.xpath("//a"));
			for (int i=0; i<listOfSubElements.size();i++)
			{
				if("Tamil New DVDRips - HDRips - BDRips (Movies)".equalsIgnoreCase(listOfSubElements.get(i).getAttribute("title").toString()))
				{
					newTamilMoviesUrl = listOfSubElements.get(i).getAttribute("href");
					System.out.println(newTamilMoviesUrl);
					break;
				}
			}
		}
		
		FileWriter writer = null;
		try 
		{
			writer = new FileWriter("E:\\Personal\\Movies_Full.txt", true);
			if(newTamilMoviesUrl != null && !newTamilMoviesUrl.isEmpty())
			{
				String pagSortedUrl = "";
				int totPages = 0;
				driver.get(newTamilMoviesUrl);
				
				List<WebElement> listOfSubElements = driver.findElements(By.xpath("//a"));
				for (int i=0; i<listOfSubElements.size();i++)
				{
					if("Start Date".equalsIgnoreCase(listOfSubElements.get(i).getText().toString()))
					{
						pagSortedUrl = listOfSubElements.get(i).getAttribute("href");
						System.out.println(pagSortedUrl);
					}
					else if(listOfSubElements.get(i).getText().toString().toLowerCase().contains("page"))
					{
						String pages = listOfSubElements.get(i).getText();
						System.out.println(pages);
						totPages = Integer.parseInt(pages.substring(10).trim());
						System.out.println(totPages);
					}
					if(pagSortedUrl != null && !pagSortedUrl.isEmpty() && totPages > 0)
					{
						break;
					}
				}
				
				if(pagSortedUrl != null && !pagSortedUrl.isEmpty() && totPages > 0)
				{
					for(int i = 2; i <= (totPages + 1); i++)
					{
						driver.get(pagSortedUrl);
						
						Thread.sleep(2000);
						writer.write("---------------------------------------------------Page " + (i-1) + "-----------------------------------------");
						writer.write("\r\n");   // write new line
						listOfSubElements = driver.findElements(By.className("topic_title"));
						for (int j=0; j<listOfSubElements.size();j++)
						{
							String title = listOfSubElements.get(j).getAttribute("title");
							String url = listOfSubElements.get(j).getAttribute("href");
							writer.write(title + " : " + url);
		                    writer.write("\r\n");   // write new line
						}
						
						if(!pagSortedUrl.toLowerCase().contains("page-"))
						{
							String pageValue = "page-" + i;
							String prevPageUrl = pagSortedUrl;
							pagSortedUrl = prevPageUrl.substring(0, prevPageUrl.indexOf("?")) + pageValue + prevPageUrl.substring(prevPageUrl.indexOf("?"));
							System.out.println(pagSortedUrl);
						}
						else
						{
							String oldPage = "page-" + (i - 1);
							String newPage = "page-" + i;
							pagSortedUrl = pagSortedUrl.replace(oldPage, newPage);
						}
					}
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(writer != null)
					writer.close();
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
	}
}
