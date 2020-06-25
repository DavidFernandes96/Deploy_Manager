# Deploy Manager

Automatic system that removes and instantiates virtual machines based on a configuration file.

System has two modes:
* **Change of configuration** - while the system is running, the user might make a change to the configuration file, so the system removes the boxes who suffered an alteration (and only those) and applies the necessary changes to them, whether that be a new IP Address or the addition/removal of an update.
It also gives the user the ability to remove or add new boxes to the system.
Whatever changes the user performs, one thing must be respected - the maximum number of machines **not** running mustn't exceed a particular value. That number is the result of a formula and it can range between 1 and higher. The formula is the following: <img src="https://render.githubusercontent.com/render/math?math=$f = \frac{n-1}{3}$">.
Where <img src="https://render.githubusercontent.com/render/math?math=n"> is the total number of machines on the DM in a particular moment and <img src="https://render.githubusercontent.com/render/math?math=f"> is the maximum number of machines that can temporary leave the the configuration.
