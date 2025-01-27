# About Windows Testing Framework

## AutomatedWindowsApp.java 
The AutomatedWindowsApp class has basic Common reusable methods for all windows applications.
It can handle actions (Click, Read,Set) using windows attribute types (AttributeID, ClassName, Name, XPath) when single and multiple windows presents. 
It can handle to Launch Windows application and server by calling loadWindowsApp() method
LoadWindowsApp() method can load, Launch application server and trigger the script and then store results in the results variable.
It can handle close windows application server by calling runWindowsScript(args) in the main method. Terminate the application based on the config file setting.
 If CLOSE_ON_TERMINATE=TRUE , then application will be closed right after the script execution. If CLOSE_ON_TERMINATE=FALSE , then application will stay after the test execution.
It can handle any object checking for invisible status based on the attribute type, when clicking object by calling waitForElementInvisibleByAccessibilityId() or waitForElementInvisibleByClassName() or waitForElementInvisibleByName() or waitForElementInvisibleByXPath()
also can handle waiting time between loading mutiple windows by calling waitForLoadMultipleWindows()


## Currently usable features
-	Testing any windows application
-       Loading and triggering script and then storing test results by calling LoadWindowsApp() method
-	Launching application server by PIC
-       Terminating application server by calling runWindowsScript(args) method from the testscript
-      Handling waiting time of element when loading multiple windows  
 
## Installation version:
WindowsApplication Driver - 1.2.1 (currently used) (we can update newer versions in the future)
  

## POM.xml Dependency _PIC
The below dependency added in PIC (we can update newer versions in the future): 

     <dependency>
      <groupId>io.appium</groupId>
      <artifactId>java-client</artifactId>
      <version>7.3.0</version>
       </dependency>


# USER REFERENCE

## Config.properties file:
User should update below fields in Config.properties file in the {application}Windows project

Windows_App= (Location of your windows application. Ex:C:\\ProgramData\\CPRS\\CPRS_Daytshr.lnk)
WindowsServer_URL=http://127.0.0.1:4723
Windows_App_Driver_Loc=(Location of your windows application driver Ex: C:\\Automation\\WinAppDriver.exe)
AppDriver_Port=4723


## WinAppDriver and WindowsKit Installation:

Process of Installation tool:
1.	User should download and install windows application driver (WindowsApplicationDriver_1.2.1.msi): 
Releases · microsoft/WinAppDriver (github.com)
2.	User should download and install – Windows 10 SDK (for Windows application attribute identification purpose): Windows SDK - Windows app development (microsoft.com)
3. 	Turn on 'Developer mode' on windows to build things and solve problems, and developer-focused solutions give us better access to the tools and environments we need to get the job done. 


## POM.xml Dependency for Windows Project

User has to add this below dependency in the POM.XML file in {application}Windows project 

      <dependency>
			<groupId>org.seleniumhq.selenium</groupId>
			<artifactId>selenium-java</artifactId>
			<version>3.141.59</version>
		</dependency>



