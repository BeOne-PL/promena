package pl.beone.promena.alfresco.module.core.internal.transformation

internal class MaxSizeHashMap<K, V>(
    private val maxSize: Int
) : LinkedHashMap<K, V>() {

    override fun removeEldestEntry(eldest: Map.Entry<K, V>): Boolean {
        return size > maxSize
    }
}