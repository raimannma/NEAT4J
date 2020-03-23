# NEAT4J
[![Build Status](https://travis-ci.org/raimannma/NEAT4J.svg?branch=master)](https://travis-ci.org/raimannma/NEAT4J)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/00e0f31c53304ca08ab8b67c3743b436)](https://app.codacy.com/manual/raimannma/NEAT4J?utm_source=github.com&utm_medium=referral&utm_content=raimannma/NEAT4J&utm_campaign=Badge_Grade_Settings)
<a href="/LICENSE">
  <img src="https://img.shields.io/github/license/raimannma/NEAT4J" alt="NEAT4J's License">
</a>
<a href="https://github.com/raimannma/NEAT4J/graphs/contributors">
  <img src="https://img.shields.io/github/contributors/raimannma/NEAT4J">
</a>

[Javadocs](https://raimannma.github.io/NEAT4J/)

This is an architecture-free neural network library, which uses the [instinct algorithm](https://towardsdatascience.com/neuro-evolution-on-steroids-82bd14ddc2f6) to evolve the best possible neural network.

## Getting Started

You can simply use the jar file of the latest [release](https://github.com/raimannma/NEAT4J/releases) or build it from source (see below).

### Examples

#### Create a network

    Network network = new Network(numInputs, numOutputs);
#### Learn the AND-Gate

    final double[][] inputs = new double[][]{  
	    new double[]{0, 0},  
		new double[]{0, 1},  
	    new double[]{1, 0},  
	    new double[]{1, 1},  
    };  
    final double[][] outputs = new double[][]{  
	    new double[]{0},  
	    new double[]{0},  
	    new double[]{0},  
	    new double[]{1},  
    };  
      
    final Network network = new Network(2, 1); // create minimal network
    
    final EvolveOptions options = new EvolveOptions();  
    options.setError(0.05); // set target error for evolution
    // more options see here: https://raimannma.github.io/NEAT4J/architecture/EvolveOptions.html

    network.evolve(inputs, outputs, options); // evolve the network

## Running the tests

    git clone https://github.com/raimannma/NEAT4J.git
    cd NEAT4J
    mvn test

## Build from source

    git clone https://github.com/raimannma/NEAT4J.git
    cd NEAT4J
    mvn clean package

## Contributing

Please read [CONTRIBUTING.md](https://github.com/raimannma/NEAT4J/blob/master/CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Authors

[Manuel Raimann](https://github.com/raimannma) *Initial work*

See also the list of [contributors](https://github.com/raimannma/NEAT4J/graphs/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE](https://github.com/raimannma/NEAT4J/blob/master/LICENSE) file for details

## Acknowledgments
Heavily inspired by [carrot](https://github.com/liquidcarrot/carrot)