package app.template.patches.pizzabusiness

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest

/** Exact 5.58.0 layout. Adds a per-NewsLayer counter, never a saved/global cap. */
internal object PizzaPnnNative {
    private const val ADDRESS = 0x23e0000L
    private val acceptedHashes = setOf(
        "937378e4e01db235a19944746ae9c311f73df0de1f1ee7eae5e6cc5745e9fdd3",
        "e201dee34e1869316c1e6b3ddf0e2713743688d5cf2587d759a997363a563e60",
    )

    fun apply(original: ByteArray, payload: ByteArray): ByteArray {
        val hash = MessageDigest.getInstance("SHA-256").digest(original)
            .joinToString("") { "%02x".format(it) }
        require(hash in acceptedHashes) { "PNN 1000 requires the restored Pizza 5.58.0 ARM64 library." }
        require(payload.size == 64) { "Invalid PNN payload size" }
        val source = ByteBuffer.wrap(original).order(ByteOrder.LITTLE_ENDIAN)
        require(source.getInt(0) == 0x464c457f && original[4] == 2.toByte() &&
            original[5] == 1.toByte() && source.getShort(18).toInt() == 183)
        val headers = source.getLong(32).toInt()
        val count = source.getShort(56).toInt()
        require(headers == 64 && count == 14 && source.getShort(54).toInt() == 56)
        val extra = headers + count * 56
        require(original.sliceArray(extra until extra + 56).all { it == 0.toByte() })
        val offset = (original.size + 0x3fff) and -0x4000
        val output = original.copyOf(offset + payload.size)
        val target = ByteBuffer.wrap(output).order(ByteOrder.LITTLE_ENDIAN)
        var phdr = false
        repeat(count) { index ->
            val header = headers + index * 56
            when (source.getInt(header)) {
                1 -> require(source.getLong(header + 16) + source.getLong(header + 40) <= ADDRESS)
                6 -> {
                    phdr = true
                    target.putLong(header + 32, (count + 1) * 56L)
                    target.putLong(header + 40, (count + 1) * 56L)
                }
            }
        }
        require(phdr)
        target.putShort(56, (count + 1).toShort())
        target.putInt(extra, 1) // PT_LOAD
        target.putInt(extra + 4, 5) // Read/execute; no writable executable memory.
        target.putLong(extra + 8, offset.toLong())
        target.putLong(extra + 16, ADDRESS)
        target.putLong(extra + 24, ADDRESS)
        target.putLong(extra + 32, payload.size.toLong())
        target.putLong(extra + 40, payload.size.toLong())
        target.putLong(extra + 48, 0x4000L)
        payload.copyInto(output, offset)

        fun edit(at: Int, before: Int, after: Int) {
            require(source.getInt(at) == before) { "Pizza PNN layout changed at ${at.toString(16)}" }
            target.putInt(at, after)
        }
        // One allocation/zeroing path and its three sized-delete paths.
        // The added counter is at +0x520, outside every original game member.
        edit(0x135ea20, 0x5280a400, 0x5280a500) // new 0x520 -> 0x528
        edit(0x135ea34, 0x5280a402, 0x5280a502) // memset length
        listOf(0x135eb30, 0x135ef24, 0x135ef50).forEach {
            edit(it, 0x5280a401, 0x5280a501) // sized delete
        }
        fun branch(at: Int, before: Int, destination: Long) {
            val distance = destination - at
            require(distance % 4 == 0L && distance in -(1L shl 27) until (1L shl 27))
            edit(at, before, 0x14000000 or ((distance / 4).toInt() and 0x03ffffff))
        }
        branch(0x136c4d8, 0x11000402, ADDRESS) // Successful PNN callback only.
        branch(0x1369aa0, 0xd0ffa1e1.toInt(), ADDRESS + 32) // PNN -> next placement.
        return output
    }
}
