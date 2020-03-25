package com.github.raimannma.rl.agents;

public interface ContinuousAgent extends Agent {
	double[] act(double[] state);

	double learn(double reward);
}
