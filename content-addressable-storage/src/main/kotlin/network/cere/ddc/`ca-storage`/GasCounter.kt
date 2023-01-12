package network.cere.ddc.`ca-storage`

class GasCounter {
    var commitId = 1

    val commits = HashMap<Int, Set<LongArray>>()

    val engine = HashSet<LongArray>()

    fun push(gasAmount: Long) {
        engine.add(longArrayOf(gasAmount))
    }

    fun readUncommitted(): Pair<Long, Int> {
        this.commitId += 1
        val commit = HashSet<LongArray>()
        val gasAmount = engine.map {
            commit.add(it)
            it[0]
        }.reduce { a, b -> a + b }
        engine.clear()
        commits[commitId] = commit
        return Pair(gasAmount, commitId)
    }

    fun commit(commitId: Int) {
        this.commits.remove(commitId)
    }

    fun revert(commitId: Int) {
        val commit = commits.get(commitId)
        if (!commit.isNullOrEmpty()) {
            commit.forEach { engine.add(it) }
        }
        this.commits.remove(commitId)
    }
}