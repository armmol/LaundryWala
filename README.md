# LaundryWala
Home Delivery Laundry Service Application
This Application is being developed as a part of personal project and a final degree project at the Kaunas University of Technology.
The following Technologies are required to run the application - 
**1.** Android Studio
Android Studio is the official integrated development environment (IDE) for Google's Android operating system, built on JetBrains' 
IntelliJ IDEA software and designed specifically for Android development. It is available for download on Windows, 
macOS and Linux based operating systems or as a subscription-based service in 2020. 
It is a replacement for the Eclipse Android Development Tools (E-ADT) as the primary IDE for native Android application development.
Android Studio was announced on May 16, 2013, at the Google I/O conference. It was in early access preview stage starting 
from version 0.1 in May 2013, then entered beta stage starting from version 0.8 which was released in June 2014. The first stable build was released in December 2014, starting from version 1.0.
Download Link for Android Studio - https://developer.android.com/studio
For the development process of this applicaiton - Android Studio Bumblebee 2021.1.1 was used.
**2.** Android Emmulator or an Android Emmulator or Android Device
An android studio emmulator such as BlueStacks or the Android AVD integrated in the Andorid studio platform can be used to run the application.
An android device with developer mode enabled can also be used to install this applcation after cloning the repository.
The following steps are required to clone the repository on you device - 
**1.**On github web, go to the repo you want yo clone and click on the download button (code) then copy the url where it says clone with https.
**2.**In Android Studio , go to plugs and add the github plugin.
**3.**Go to VCS then click on Get From Version Control, it will load a window where you'll paste in the url you got from github.
**4.**Dependencies will need to be updated if the repository is cloned at a later stage.

**FIREBASE**
To access the Firebase Database and provide own Authentication, the Tools option in Android Studio and connect the application to Firebae.
Install or update Android Studio to its latest version.
Make sure that your project meets these requirements:
Targets API level 19 (KitKat) or higher
Uses Android 4.4 or higher
Uses Jetpack (AndroidX), which includes meeting these version requirements:
com.android.tools.build:gradle v3.2.1 or later
compileSdkVersion 28 or later
Set up a physical device or use an emulator to run your app.
Note that Firebase SDKs with a dependency on Google Play services require the device or emulator to have Google Play services installed.
Sign into Firebase using your Google account.
If you don't already have an Android project and just want to try out a Firebase product, you can download one of our quickstart samples.
You can connect your Android app to Firebase using one of the following options:
Option 1: (recommended) Use the Firebase console setup workflow.
Option 2: Use the Android Studio Firebase Assistant (may require additional configuration).
Option 1: Add Firebase using the Firebase console
Adding Firebase to your app involves tasks both in the Firebase console and in your open Android project (for example, you download Firebase config files from the console, then move them into your Android project).
Step 1: Create a Firebase project
Before you can add Firebase to your Android app, you need to create a Firebase project to connect to your Android app. Visit Understand Firebase Projects to learn more about Firebase projects.
Create a Firebase project
Step 2: Register your app with Firebase
To use Firebase in your Android app, you need to register your app with your Firebase project. Registering your app is often called "adding" your app to your project.
In the center of the project overview page, click the Android icon (plat_android) or Add app to launch the setup workflow.
Enter your app's package name in the Android package name field.
What's a package name, and where do you find it?
Make sure to enter the package name that your app is actually using. The package name value is case-sensitive, and it cannot be changed for this Firebase Android app after it's registered with your Firebase project.
(Optional) Enter other app information: App nickname and Debug signing certificate SHA-1.
How are the App nickname and the Debug signing certificate SHA-1 used within Firebase?
Click Register app.
Step 3: Add a Firebase configuration file
Add the Firebase Android configuration file to your app:
Click Download google-services.json to obtain your Firebase Android config file (google-services.json).
Move your config file into the module (app-level) directory of your app.
What do you need to know about this config file?
To enable Firebase products in your app, add the google-services plugin to your Gradle files.
In your root-level (project-level) Gradle file (build.gradle), add rules to include the Google Services Gradle plugin. Check that you have Google's Maven repository, as well.
Step 4: Add Firebase SDKs to your app
Using the Firebase Android BoM, declare the dependencies for the Firebase products that you want to use in your app. Declare them in your module (app-level) Gradle file (usually app/build.gradle).
Option 2: Add Firebase using the Firebase Assistant
The Firebase Assistant registers your app with a Firebase project and adds the necessary Firebase files, plugins, and dependencies to your Android project — all from within Android Studio!

Technolgies used from Firebase - 
1) Firebase Authentication - E-mail and google sign in is authorised for users(Customers,Couriers,Laundry Houses) to register and login into theri accounts.
2) Firebase FireStore Cloud Storage Database - Data base is used to save data from the app like orders and data about users and access live tracking when connected to MAPS SDK for Andorid to open google maps in the applcation. 

Firebase Android API reference makes use of the following packages in the application –
1.	firebase.auth – com.google.firebase.auth
2.	firebase.firestore - com.google.firebase.firestore
To authenticate users using Google Sign in, the firebase console requires an OAuth 2.0 Client ID of the web application type to be provided to it by the application. This Client ID is automatically created by Google Service in the Google Cloud Platform when connecting to Firebase via Android Studio.

**GOOGLE CLOUD PLATFORM**
Google Cloud Platform is a subset of Google Cloud, which contains the Google Cloud Platform public cloud architecture, Google Workspace (G Suite), corporate versions of Android and Chrome OS, and machine learning and enterprise mapping APIs [25]. The application uses many APIs provided by the platform. Some of the major APIs in use include Maps SDK for Android for accessing Google Maps on the application, Cloud Firestore API to read and write data to the Firebase Cloud Firestore database and Places API to search and set application user address.
The platform is also used to provide the OAuth 2.0 web Client ID to authenticate Google sign in as mentioned before.

**ARCHITECTURE OF APPLICAITON**
**MVVM - Model View View-Model**
MVVM is divided into View, ViewModel and Model:
View is the collection of visible elements, which also receives user input. This includes user interfaces (UI), animations and text. The content of View is not interacted with directly to change what is presented.
ViewModel is located between the View and Model layers. This is where the controls for interacting with View are housed, while binding is used to connect the UI elements in View to the controls in ViewModel.
Model houses the logic for the program, which is retrieved by the ViewModel upon its own receipt of input from the user through View.




