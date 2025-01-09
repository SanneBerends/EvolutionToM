# Simulation of the Evolution of Theory of Mind

This Github repository contains the code used for the simulations and data processing of my graduation project titled The Evolution of Theory of Mind in a Simulated
Population: a Mixed-Motive Setting. 
The code can be used to run the first and second experiment as described in the project report. Which experiment
to run must be specified by the user, see the section Running for an explanation. The results generated by the 
program can be visualized (see Data processing).

## Running
The relevant code is located in the _src/main/java_ folder.
Java Development Kit version 21 is required to compile and run the code. Furthermore, the simulation uses JFreeChart. 
The root directory contains a pom.xml file that can be used to use download and use these dependensies. For this, Maven is needed.
A tutorial on how to install Maven can be found [here](https://www.baeldung.com/install-maven-on-windows-linux-mac).

Once Maven is installed, clone this _EvolutionToM_ repository, and then run 
```text
mvn clean install
```
from the command line in the root directory.
This compiles the project, runs tests, and packages it into a .jar file in the target directory, ready for execution.

The simulation requires three input parameters: the first one specifies whether or not to enable the GUI (true or false), 
the second one specifies which experiment to run (1 or 2), and the third specifies the name of the file to which the results are written. 

The simulation can then be run using:
```text
mvn exec:java -Dexec.mainClass="Main" -Dexec.args="arg1 arg2 arg3"
```
so an example is: 
```text
mvn exec:java -Dexec.mainClass="Main" -Dexec.args="true 1 results"
```
which runs experiment 1 with the GUI, and which saves the results to a file named _results_0.csv_ in the _EvolutionToM_ directory.


## Data processing
To visualize the data that were gathered with the experiments, you can use the _processData.py_ and the _processDataExp2.py_ files. 
These files are located in the folder _finalResults/quantitative/exp1_ and _finalResults/quantitative/exp2_ respectively. 
The necessary dependences can be installed by running 
```text
pip3 install -r requirements.txt
```
in both experiment folders. Note that **Python3.10** is required for these dependencies. It is also adviced to use a virtual environment (see [this link](https://docs.conda.io/projects/conda/en/latest/user-guide/tasks/manage-environments.html)).

The processing programs are now ready to be used. They requires an input argument, which is the number of experiments that were run per learning speed. Note that this number is assumed to be the same for
each $\\lambda$. In the presented research, this number was 120. 

The prgram can be run by using:
```text
python3 processData.py 120
```
The graphs are automatically saved to the _exp1_ or _exp2_ folder.


## Implementation
Note that the simulation used Theory of Mind (ToM). The implementation of this ToM was based on the descriptions by 
``` text
de Weerd, H., Verbrugge, R. & Verheij, B. Negotiating with other minds: the role of recursive theory of mind in negotiation with incomplete information. Auton Agent Multi-Agent Syst 31, 250–287 (2017).
```
An explanation of the adaptions that I made to convert the Collored Trails setting to a trading setting can be found in the project report.

