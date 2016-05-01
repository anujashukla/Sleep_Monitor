# Sleep_Monitor
This app is used to detect the inactiveness of the user or in other words, for how long the mobile phone was not moved by the user.

What is Inactiveness : If the phone is not moved for more than an hour, then it will be considered as inactiveness.
Time interval between two inactiveness slots : If the user has not moved the phone for 1 hour two times with a time gap of less than 15 minutes, then it will be considered as a total of 2 hours and X mins inactiveness, where X is the time gap mentioned earlier.

In case, the time gap exceeds 15 mins, then these two 1 hour slots will be considered as two different blocks of inactiveness.

The app has two options 
1. Sample data 
2. Actual data

The above mentioned design is nothing but the actual data of the app, while in sample data, I have changed 1 hour to 1minute and 15 minutes to 15 seconds, for the sake of fast testing.



