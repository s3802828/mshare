# MShare - Music Sharing App

Android app to enable users to share their current listening songs to the other and then both of them could listen to it at the same time. Users can also chat together while sharing. 

Contents
========

* [App Functionalities](#app-functionalities)
* [Technologies](#technologies)
* [Open Issues and Known Bugs](#open-issues-and-known-bugs)

### App Functionalities
---

	- Create music rooms and invite other users to listen together (by sending request notification)
	- Upload new songs for sharing later (file is from sdcard folder)
	- Live chat during the sharing music session
	- Message notification (can view the conversation directly or direct reply on the message notification)
	- Edit the favorite songs, genre or artist on profile page
	- Check all conversation history
	- Multiple supporting login platforms: email (include email verification), Facebook, Google
	- Sign up
	- Search available songs or users based on name 
	- Upload and edit user avatar + edit user name on profile page (file is from sdcard folder).

### Technologies
---

	- Firebase (Firestore, Authentication, Storage): mainly to store data
	- S3 storage: this is to store the default songs and images so in case the Firebase Storage is out of quota
	- Glide: fetch image from URL to imageView
	- Retrofit: to query API of firebase cloud messaging to send notification from one device to another.
	- BroadcastReceiver:
		+ 1 broadcast receiver to receive intent from notification and process the response for the request notification
		+ 1 broadcast receiver to handle the direct input to the message notification.
	- Service:
		+ 1 service to receive message from sender in firebase cloud messaging API to pop up the notification in receiver device.
		+ 1 bound service to play music.
	- Other Android components: activities, media player, notification, UI, custom adapters, etc.

### Open Issues and Known Bugs:
---
  - Firebase Storage quota limit: this is limit for free account so that sometimes, the data files in the storage cannot be fetch due to policy of Google about
    the quota limit of the firebase storage (quota limit is the maximum operation turn that we can work with storage per minute, per hour, per day, per month).
    But the quota limit exception is only raised in a defined amount of time. After that, it would be normal again.So that we have work with Storage and S3 storage together
    in case Firebase Storage has run out of the quota limit.
  - Notification: In order to send notification from 1 device to another, we decide to use Retrofit to make a POST to the API from firebase cloud messaging. However,
    based on the enQueue function or the message received function from firebase cloud messaging service or the internet connection, the notification would be sent very slow. 
    In this case, we often re-run/re-install the app again and again so that the notification could be work as normal
  - Syncing function: based on our algorithm, we just update the mutual information (current_duration of song, current_song position, etc) at some particular point
    of time in the sharing mode (when begin the sharing mode, when change the songs, when pause the song, etc) so that based on the internet connection, the speed of each emulator, the time playing from 2 devices could be different from each other around 0-3 seconds. To our expectation, we thought this error range is acceptable.
  - In LoginActivity, there is a "red" attribute called "default_web_client_id", this is for google sign in. This error is just notified by Android Studio but the app still could run normally so that this is NOT the compile error.
 - Assumption: 
      + We assume user to use Android from 26 to later.
      + User use our app in the good internet connection environment. (This is not handled yet)
