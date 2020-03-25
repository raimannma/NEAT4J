package com.github.raimannma.rl.agents;

public interface DiscreteAgent extends Agent {
	int act(double[] state);

	double learn(double reward);
}
