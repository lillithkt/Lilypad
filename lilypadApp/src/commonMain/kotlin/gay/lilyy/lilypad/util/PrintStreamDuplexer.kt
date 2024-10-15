package gay.lilyy.lilypad.util

import java.io.PrintStream

class PrintStreamDuplexer(private val stream1: PrintStream, private val stream2: PrintStream) : PrintStream(stream1) {
    override fun write(buf: ByteArray, off: Int, len: Int) {
        super.write(buf, off, len)
        stream2.write(buf, off, len)
    }

    override fun write(b: Int) {
        super.write(b)
        stream2.write(b)
    }

    override fun write(b: ByteArray) {
        super.write(b)
        stream2.write(b)
    }
}