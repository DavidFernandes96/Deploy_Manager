# Deploy Manager

## Automatic system that removes and instantiates virtual machines based on a configuration file.

## System modes

* **Change of configuration** - while the system is running, the user might make a change to the configuration file, so the system removes the VMs who suffered an alteration (and only those) and applies the necessary changes to them, whether that be a new IP Address or the addition/removal of an update.
It also gives the user the ability to remove or add new boxes to the system.
Whatever changes the user performs, one thing must be respected - the maximum number of machines **not** running mustn't exceed a particular value. That number is the result of a formula and it can range between 1 and higher. The formula is the following: <img src="https://render.githubusercontent.com/render/math?math=$f = \frac{n-1}{3}$">.
Where <img src="https://render.githubusercontent.com/render/math?math=n"> is the total number of machines on the DM in a particular moment and <img src="https://render.githubusercontent.com/render/math?math=f"> is the maximum number of machines that can temporary leave the the configuration.
* **Update** - a mode which is 100% autonomous. All machines need to be updated at some point even without the intervention of the user. So a schedule is defined (for a typical use would be once a week) and the system automatically removes all the VMs one by one and updates them. However, the same number of machines must keep running at all costs so in order to do that, a backup VM is previously established at the start of the program. While one of the machines is updating, that backup VM takes its place. The process is repeated with all the remaining VMs.


## **Software required**

[Vagrant](https://www.vagrantup.com) and Virtual Box.


## **How to use**

After installing both Vagrant and Virtual Box, go to the project directory and create a file with the name *config.txt*. Inside this file add as many vagrant boxes as you want (take a look at [this](https://app.vagrantup.com/boxes/search) list).
Each line must follow a particular rule:

```
<Name of the box e.g "ubuntu/trusty64">;<hostname>;<IP Address>;[update 1];[update 2];(...);[update n]
```
If you need help check out the example here with the file with same name.

Compile and execute:
```
$javac Main.java
```
```
$java Main
```
