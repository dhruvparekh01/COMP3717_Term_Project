README

SET 3M - Team: Panthera
Philip Bolinao, A00953572
Dhruv Parekh, A01157984
Jihyo Kim, A01017545

App Category: Safety
City: New Westminster, BC

App Name: SafeTraveler
App Idea: A mobile navigation app that displays the speed limits for the street
that the user is currently on.

Dataset: http://opendata.newwestcity.ca/datasets/speed-signs

Application Walkthrough:
	1) Upon opening the app there will be a text box and a button
		-Fill in the text box to select a destination
		-Click "Start Route" to begin navigation
	2) As you drive, a box in the top right will display the current speed limit for the street you are on.
		-Depending on your speed the colour of the box will change
			-When the box is green it means that you are safely following the speed limit
			-When the box turns yellow it can either mean:
			    -You are going slightly over the speed limit
			    -You are going slightly under the speed limit
			-When the box turns read it means you are speeding!
	3) At the bottom it will display the users current speed.
		-An X button is also present for ending navigation
-Ensure that your volume is on to hear Text-to-Speech warnings.

-Speed sign data is limited to New Westminster, BC
    Some Notable Points (For ease in searching specific locations in the city and testing)
        -Royal Columbian Hospital
            -330 E Columbia St, New Westminster, BC V3L 3W7
        -New Westminster Public Library
            -716 6th Ave, New Westminster, BC V3M 2B3
        -Victoria Sushi
            -15 E Royal Ave #10, New Westminster, BC V3L 0A9
        -New Westminster Secondary School
            -835 8th St, New Westminster, BC V3M 3S9
        -Justice Institute of British Columbia (JIBC)
            -715 McBride Blvd, New Westminster, BC V3L 5T4

Issues:
-A null pointer exception occurs on the Pixel 3 Api 28 emulator.
    -When using the autocomplete feature while searching for a destination,
     it may crash.
	-Can be fixed by using the Pixel 3 Api 29
	-According to https://stackoverflow.com/questions/5205650/geocoder-getfromlocation-throws-ioexception-on-android-emulator
	This is an emulator only error.
	-This error did not occur when tested on a physical Samsung Galaxy S7
-Upon initial install, the emulator may set the Google HQ as the users location despite
 having set the location in the options. This is an emulator issue, and can be resolved by
 closing the app entirely then restarting the app.
    -This error did not occur when tested on a physical Samsung Galaxy S7
-This project was made using Android Studio 3.6.2

