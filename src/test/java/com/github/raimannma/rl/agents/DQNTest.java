package com.github.raimannma.rl.agents;

import java.util.LinkedList;
import org.junit.jupiter.api.Test;

class DQNTest {

	@Test
	public void testLearning() {
		final DQN dqn = new DQN(2, 2);

		double currentState = 0.5;
		final int windowSize = 100;
		final LinkedList<Double> rewardWindow = new LinkedList<>();

		while (rewardWindow.size() <= 90 || rewardWindow.stream().mapToDouble(i -> i).average().orElseThrow() < 0.9) {
			final int action = dqn.act(new double[]{Math.floor(currentState), Math.ceil(currentState)});
			final double newState = action == 1
					? Math.min(1, currentState + 0.5)
					: Math.max(0, currentState - 0.5);
			final double reward = newState != currentState ? 1 : -1;
			dqn.learn(reward, false);
			rewardWindow.add(reward);
			if (rewardWindow.size() > windowSize) {
				rewardWindow.pollFirst();
			}

			System.out.println("Episode: " + dqn.getEpisode() + "; State: " + currentState + "; Action: " + action + "; Reward: " + reward + "; AVG Reward: " + rewardWindow.stream().mapToDouble(i -> i).average().orElseThrow());
			currentState = newState;
		}
	}
}