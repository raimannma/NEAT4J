package com.github.raimannma.rl.agents;

import com.github.raimannma.nn.architecture.Network;
import com.github.raimannma.nn.methods.Utils;
import com.github.raimannma.rl.methods.DiscreteSampling;

public class DQN extends DiscreteAgent {
	DiscreteSampling sampling;
	private Network qNetwork;

	public DQN(final int numStates, final int numActions, final DiscreteSampling sampling) {
		super(numStates, numActions);
	}

	@Override
	public int act(final double[] state) {
		final int action = Utils.getMaxValueIndex(this.qNetwork.activate(state));
		return this.sampling.sample(action, this.episode, this.numActions);
	}

	@Override
	public double learn(final double reward) {
		return 0;
	}
}
