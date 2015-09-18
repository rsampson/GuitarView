**GuitarView**

Guitar View is a simple application that gives the user an animated graphical representation of how a piece of music played from a Midi file could be fingered on a guitar keyboard. The application allows the user full control of what voices in the piece will be represented on the virtual guitar's keyboard, how fast the piece is being played etc. There is a feature in the popular application "Tux Guitar" that gives a similar graphical representation however GuitarView differs in that it is much simpler and therefor easier to expand/modify and it also supports alternate guitar tunings (or alternate instruments) so that the user can see how a piece would be played with a particular tuning.

This is a Java application which should run on any platform in theory. To build the application you will need the ControlP5 library from http://www.sojamo.de/libraries/controlP5/ and the Processing core libraries from https://processing.org/. I developed it under Linux so it runs fine there. It runs poorly on Windows 7 (the animation is too slow) and not at all under Windows 10. I haven't tested it on a Mac. I slapped this program together for my own amazement so it doubtless contains many bugs. I had fun playing with it and thought others might too.

**Quick Start**

For those impatient like me, all that is required to run this is to click the blue box labeled "load_file" in the upper left of the application's window and use the file dialog that pops up to navigate to the Midi file you wish to play. After a short pause the piece will start playing and you can then try out all of the other blue rectangular controls to see what they do.

Now for the more long winded description...

**Useage**

The applications graphic representation is divided into 4 parts; "visual display controls", "play controls", "tuning controls" and an animated graphic of the guitar keyboard down at the bottom of the screen.

The heart of the user interface is the graphic representation of a guitar fretboard at the bottom of the application window. Only a one octave span of the fretboard is shown. When a piece is played, the fingering is represented visually by coloring the struck string and placing a dot of a similar color on the fret that is fingered. Each part of the piece (a.k.a "voice" or "channel") is represented with its own unique color.

In the section labeled "visual display controls" are 3 blue buttons labeled "trace", "all" and "none". These control the display of the various parts of a musical piece. When a Midi file is loaded colored buttons representing the piece's parts are displayed in this section. These buttons toggle on and off and when on enable the corresponding part. The button labeled "none" will set all of these parts or "channels" to their off state upon which nothing will be displayed in the window's guitar neck graphic. The button labeled "all" will turn all channels back on again and all pieces will be displayed using their respective color codes. The button labeled "trace" will leave a small colored dot on the guitar graphic as each note is played. This gives a persisting visual marker of all notes that have been played in the piece.

In the section labeled "play controls" there are several buttons and two sliders. The sliders are the narrow blue rectangular bars and they may be manipulated by the mouse when the left mouse button is depressed. The slider labeled "tempo" allows the tempo of the music played to be sped up or slowed down. The number in this slider is the multiplier of the tempo rate, a value of "1" being the normal rate. The other slider labeled "progress" allows  the user to advance or replay the musical piece. The buttons "pause" and "reset" pause the play and reset it back to the start of the piece respectively. The "loop" button will cause the play to loop between two different temporal points in the piece. The two points are the last two points at which the piece was paused, or in the case of a single pause, the pause and the song beginning. If the piece has not been paused then this button is non-functional.

The section labeled "tuning controls" allows the user to select how the virtual guitar is tuned. The setting of this defaults to standard guitar tuning "e a d g b e" and most users will wish to leave this setting alone. However, you can explore how a piece might be played with an alternate guitar tuning. To select a tuning use the mouse to open the blue drop down list boxes which when opened will display mouse click able alternative tunings. An examination of the Java code in file "GuitarView.java" will show that adding additional tunings (or instruments) to the program is very easy.

It should be noted that what the program displays as a fingering is not necessarily the same fingering that a skilled human guitar player would use, but only one of the many possible fingerings that could be used to play a note. The choice of fingerings is made by the function "findClosestFingering" in file "GuitarChannel.java" and could be replaced by a much more intelligent algorithm than what I have used. In my implementation a selection of fret positions is made by choosing the fret position that is closest to the last one played.

**License**

(The MIT License)

Copyright (c) 2015 Richard Allen Sampson

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the 'Software'), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

