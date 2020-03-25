package com.github.raimannma.rl.methods;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class ReplayBuffer {
	private final int maxSize;
	protected TreeSet<Experience> buffer;

	ReplayBuffer(final int size) {
		this.buffer = new TreeSet<>();
		this.maxSize = size;
	}

	public void addExperience(final Experience experience) {
		this.buffer.add(experience);
		if (this.buffer.size() > this.maxSize) {
			this.buffer.pollFirst();
		}
	}

	public Set<Experience> getBatch(final Sampler sampler, final int batchSize) {
		if (batchSize >= this.buffer.size()) {
			return new HashSet<>(this.buffer);
		}
		final HashSet<Experience> out = new HashSet<>();
		while (out.size() < batchSize) {
			out.add(sampler.select(this.buffer));
		}
		return out;
	}
}
