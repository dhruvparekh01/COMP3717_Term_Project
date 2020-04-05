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
			-When the box turns read it means you are speeding!
	3) At the bottom it will display the users current speed.
		-An X button is also present for ending navigation

Issues:
A null pointer exception occurs on the Pixel 3 Api 28 emulator.
	-Can be fixed by using the Pixel 3 Api 29
	-According to https://stackoverflow.com/questions/5205650/geocoder-getfromlocation-throws-ioexception-on-android-emulator
	This is an emulator only error.
