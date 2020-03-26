package com.github.raimannma.rl.methods;

import static com.github.raimannma.nn.methods.Utils.randDouble;
import static com.github.raimannma.nn.methods.Utils.randInt;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ReplayBufferTest {

	@Test
	public void testSize() {
		final int size = randInt(10, 100);
		final ReplayBuffer replayBuffer = new ReplayBuffer(size);

		for (int i = 0; i < 1000; i++) {
			final Experience experience = new Experience();
			experience.setTdError(randDouble());
			replayBuffer.addExperience(experience);
			assertTrue(replayBuffer.buffer.size() <= size);
		}
	}

	@Test
	public void testSort() {
		final int size = randInt(10, 100);
		final ReplayBuffer replayBuffer = new ReplayBuffer(size);

		for (int i = 0; i < 1000; i++) {
			final Experience experience = new Experience();
			experience.setTdError(randDouble());
			replayBuffer.addExperience(experience);

			int lastGeneration = replayBuffer.buffer.first().getEpisode();
			for (final Experience exp : replayBuffer.buffer) {
				assertTrue(exp.getEpisode() >= lastGeneration);
				lastGeneration = exp.getEpisode();
			}
		}
	}

	@Test
	public void testRandomBatch() {
		final int size = randInt(10, 100);
		final ReplayBuffer replayBuffer = new ReplayBuffer(size);
		final Sampler sampler = new Sampler.RandomSampler();

		for (int i = 0; i < 1000; i++) {
			final Experience experience = new Experience();
			experience.setTdError(randDouble());
			replayBuffer.addExperience(experience);

			final int batchSize = randInt(20, 100);
			final Set<Experience> batch = replayBuffer.getBatch(sampler, batchSize);
			assertTrue(batch.size() <= batchSize);
			assertTrue(replayBuffer.buffer.containsAll(batch));
		}
	}

	@Test
	public void testPrioritizedBatch() {
		final int size = randInt(10, 100);
		final ReplayBuffer replayBuffer = new ReplayBuffer(size);
		final Sampler sampler = new Sampler.PrioritisedSampler(randDouble());

		for (int i = 0; i < 1000; i++) {
			final Experience experience = new Experience();
			experience.setTdError(randDouble());
			replayBuffer.addExperience(experience);

			final int batchSize = randInt(10, 20);
			final Set<Experience> batch = replayBuffer.getBatch(sampler, batchSize);

			assertTrue(batch.size() <= batchSize);
			assertTrue(replayBuffer.buffer.containsAll(batch));
		}
	}

	@Test
	public void testTournamentBatch() {
		final int size = randInt(20, 100);
		final ReplayBuffer replayBuffer = new ReplayBuffer(size);
		final Sampler sampler = new Sampler.TournamentSampler(randInt(2, 4));

		for (int i = 0; i < 1000; i++) {
			final Experience experience = new Experience();
			experience.setTdError(randDouble());
			replayBuffer.addExperience(experience);

			final int batchSize = randInt(10, 20);
			final Set<Experience> batch = replayBuffer.getBatch(sampler, batchSize);

			assertTrue(batch.size() <= batchSize);
			assertTrue(replayBuffer.buffer.containsAll(batch));
		}
	}

	@Test
	public void testPowerBatch() {
		final int size = randInt(10, 100);
		final ReplayBuffer replayBuffer = new ReplayBuffer(size);
		final Sampler sampler = new Sampler.PowerSampler(randInt(1, 10));

		for (int i = 0; i < 1000; i++) {
			final Experience experience = new Experience();
			experience.setTdError(randDouble());
			replayBuffer.addExperience(experience);

			final int batchSize = randInt(10, 20);
			final Set<Experience> batch = replayBuffer.getBatch(sampler, batchSize);

			assertTrue(batch.size() <= batchSize);
			assertTrue(replayBuffer.buffer.containsAll(batch));
		}
	}

	@Test
	public void testFitnessProportionateBatch() {
		final int size = randInt(10, 100);
		final ReplayBuffer replayBuffer = new ReplayBuffer(size);
		final Sampler sampler = new Sampler.FitnessProportionateSampler();

		for (int i = 0; i < 1000; i++) {
			final Experience experience = new Experience();
			experience.setTdError(randDouble());
			replayBuffer.addExperience(experience);

			final int batchSize = randInt(10, 20);
			final Set<Experience> batch = replayBuffer.getBatch(sampler, batchSize);

			assertTrue(batch.size() <= batchSize);
			assertTrue(replayBuffer.buffer.containsAll(batch));
		}
	}
}