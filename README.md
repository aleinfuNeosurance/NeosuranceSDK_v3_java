# ![](https://upload.wikimedia.org/wikipedia/commons/thumb/d/d7/Android_robot.svg/32px-Android_robot.svg.png) Android - Neosurance SDK v3 Java

- Collects info from device sensors and from the hosting app
- Exchanges info with the AI engines
- Sends push notifications
- Displays a landing page
- Displays a list of purchased policies


## Installation

NeosuranceSDK is available through [jitpack](https://jitpack.io/). To install
it, simply add the following line to your project (see also [NSR SDK v3 JavaExample](https://github.com/aleinfuNeosurance/NeosuranceSDK_v3_java_example)):

1. Add it in your root **gradle** file at the end of repositories:

	```gradle
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
	```

2. Then add the dependency in your application **gradle** file

	```gradle
	dependencies {
		...
		implementation 'com.github.aleinfuNeosurance:NeosuranceSDK_v3_java:3.0.11'
	}
	```

## Requirements (be sure)

1. Inside your project **AndroidManifest.xml** be sure to have the following permissions:

	```xml
	<uses-permission android:name="android.permission.INTERNET" />
	<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
	<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
	<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
	<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
	<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
	<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
	<uses-permission android:name="android.permission.CAMERA" />
	<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
	<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
	<uses-permission android:name="com.google.android.gms.permission.ACTIVITY_RECOGNITION" />
	```
2. Inside NSR SDK library **NeosuranceSDK_v3_java/src/main/AndroidManifest.xml** be sure to have the following statements:

	```xml
	<receiver android:name="NSRBootReceiver">
		<intent-filter>
			<action android:name="android.intent.action.BOOT_COMPLETED" />
		</intent-filter>
	</receiver>
	```
	
	```xml
	<receiver android:name="NSRActivityCallback" />
	<receiver android:name="NSRDelayedPush" />
	<receiver android:name="NSRBackground" />
	
    <receiver android:name="eu.nsrsdk.utils.PackageChangeReceiver">
        <intent-filter>
            <action android:name="android.intent.action.PACKAGE_REMOVED"/>
            <action android:name="android.intent.action.PACKAGE_FULLY_REMOVED" />
            <action android:name="android.intent.action.ACTION_PACKAGE_REMOVED"/>
            <data android:scheme="package"/>
        </intent-filter>
    </receiver>
	```
	
	```xml
	<service android:name="NSRForeground" android:exported="false" />
	```
	
	```xml
    <provider android:name="androidx.core.content.FileProvider" android:authorities="${applicationId}.provider" android:exported="false" android:grantUriPermissions="true">
        <meta-data android:name="android.support.FILE_PROVIDER_PATHS" android:resource="@xml/file_paths" />
    </provider>
	```


3. Inside your **proguard-rules.pro** be sure to have the following rules:
	
	```
	-keep class eu.neosurance.sdk.** { *; }
	-keep class * implements eu.neosurance.sdk.NSRSecurityDelegate { *; }
	-keep class * implements eu.neosurance.sdk.NSRWorkflowDelegate { *; }
	-keep class * implements eu.neosurance.sdk.NSRPushDelegate { *; }
	```

## Settings

1. ### setup
	Earlier in your application startup flow (tipically inside the **onCreate** method of your main activity) call the **setup** method using

		
	```java
	NSRSettings settings = new NSRSettings();
	settings.setDisableLog(false);
	settings.setDevMode(true);
	settings.setBaseUrl("<base_url>");
	settings.setCode("<code>");
	settings.setSecretKey("<secret_key>");
	settings.setWorkflowDelegate(new WFDelegate(),getApplicationContext());
	NSR.getInstance(this).setup(settings,new JSONObject());
	NSR.getInstance(this).askPermissions(this);
	```
2. ### setSecurityDelegate *optional*
	If the communications must be secured using any policy.  
	A **securityDelegate** implementing the following interface can be configured:
	
	```java
	public interface NSRSecurityDelegate {
		void secureRequest(Context ctx, String endpoint, JSONObject payload, JSONObject headers, NSRSecurityResponse completionHandler) throws Exception;
	}
	```
	It's *mandatory* that your **securityDelegate** implements the **default constructor** and must be excluded from any obfuscation (ProGuard).  
	Then use the ***setSecurityDelegate*** method
	
	```java
	NSR.getInstance(this).setSecurityDelegate(<yourSecurityDelegate>);
	```
	
3. ### setWorkFlowDelegate *optional*  
	If the purchase workflow must be interrupted in order to perform user login or to perform payment.  
	A **workflowDelegate** implementing the following interface must be configured:
	
	```java
	public interface NSRWorkflowDelegate {
		boolean executeLogin(Context ctx, String url);
		JSONObject executePayment(Context ctx, JSONObject payment, String url);
		void confirmTransaction(Context ctx, JSONObject paymentInfo);
	}
	```
	
	It's *mandatory* that your **workflowDelegate** implements the **default constructor** and must be excluded from any obfuscation (ProGuard).  
	Then use the ***setWorkflowDelegate*** method

	```java
	NSR.getInstance(this).setWorkflowDelegate(<yourWorkflowDelegate>);
	```
	
	when login or payment is performed you must call the methods **loginExecuted** and **paymentExecuted** to resume the workflow
	
	```java
	NSR.getInstance(this).loginExecuted(<theGivenUrl>);
	...
	NSR.getInstance(this).paymentExecuted(<paymentTransactionInfo>,<theGivenUrl>);
	```
	
4. ### setPushDelegate *optional*   
	If you want intercept the push interaction a **pushDelegate** implementing the following interface can be configured:
	
	```java
	public interface NSRPushDelegate {
		PendingIntent makePendingIntent(Context ctx, JSONObject push);
	}
	```
	The task of this delegate is to provide the **PendingIntent** associated with the push.  
	The ***push*** parameter contains all the relevant informations.  
	It's *mandatory* that your **pushDelegate** implements the **default constructor** and must be excluded from any obfuscation (ProGuard).  
	Then use the ***setPushDelegate*** method
	
	```java
	NSR.getInstance(this).setPushDelegate(<yourPushDelegate>);
	```
	
5. ### Register User  
	When the user is recognized by your application, register him in our *SDK* creating an **NSRUser** and using the **registerUser** method.  
	The **NSRUser** has the following fields:
	
	**code**: the user code in your system (can be equals to the email)  
	**email**: the email is the real primary key  
	**firstname** *optional*  
	**lastname** *optional*  
	**mobile** *optional*  
	**fiscalCode** *optional*  
	**gender** *optional*  
	**birthday** *optional*  
	**address** *optional*  
	**zipCode** *optional*  
	**city** *optional*  
	**stateProvince** *optional*  
	**country** *optional*  
	**extra** *optional*: will be shared with us  
	**locals** *optional*: will not be exposed outside the device  
	
	```java
	NSRUser user = new NSRUser();
	user.setEmail("<email>"));
	user.setCode("<code>"));
	user.setFirstname("<firstname>"));
	user.setLastname("<lastname>"));
	
	JSONObject locals = new JSONObject();
	locals.put("private_email","<private_email>");
	locals.put("private_firstname","<private_firstname>");
	locals.put("private_lastname","<private_lastname>");
	user.setLocals(locals);
	
	NSR.getInstance(this).registerUser(user);
	```
6. ### forgetUser *optional*
	If you want propagate user logout to the SDK use the **forgetUser** method.  
	Note that without user no tracking will be performed.
	
	```java
	NSR.getInstance(this).forgetUser();
	```
7. ### Show App
	Is possible to show the list of the purchased policies (*communityApp*) using the **showApp** methods
	
	```java
	NSR.getInstance(this).showApp();
	```
	or
	
	```java
	JSONObject params = new JSONObject();
	params.put("page", "profiles");
	NSR.getInstance(this).showApp(params);
	```
8. ### showUrl *optional*
	If custom web views are needed the **showUrl** methods can be used
	
	```java
	NSR.getInstance(this).showUrl(url);
	```
	or
	
	```java
	JSONObject params = new JSONObject();
	params.put("privacy", true);
	NSR.getInstance(this).showUrl(url, params);
	```
9. ### Send Event
	The application can send explicit events to the system with **sendEvent** method
	
	```java
	JSONObject payload = new JSONObject();
	payload.put("latitude", latitude);
	payload.put("longitude", longitude);
	NSR.getInstance(this).sendEvent("position", payload);
	```

## Usage (Sample Demo Flow)
1. Tap on button => *"Setup"* (see 1. [setup](#setup))
2. Tap on button => *"registerUser"* (see 5. [registerUser](#register-user))
3. [4.] Tap on button => *"sendEvent"* (and/or *"sendEventPush"*) (see 9. [sendEvent](#send-event))
4. [3.] Tap on button => *"showApp"* In order to show "Purchases List" or "buy a new insurance policy"(just tapping on the title)) (see 7. [showApp](#show-app))

## Author

info@neosurance.eu

## License

NeosuranceSDK is available under the MIT license. See the LICENSE file for more info.
