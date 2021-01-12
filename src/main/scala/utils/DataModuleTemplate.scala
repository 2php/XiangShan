package utils

import chisel3._
import chisel3.util._

class AsyncDataModuleTemplate[T <: Data](gen: T, numEntries: Int, numRead: Int, numWrite: Int) extends Module {
  val io = IO(new Bundle {
    val raddr = Vec(numRead,  Input(UInt(log2Up(numEntries).W)))
    val rdata = Vec(numRead,  Output(gen))
    val wen   = Vec(numWrite, Input(Bool()))
    val waddr = Vec(numWrite, Input(UInt(log2Up(numEntries).W)))
    val wdata = Vec(numWrite, Input(gen))
  })

  val data = Mem(numEntries, gen)

  // read ports
  for (i <- 0 until numRead) {
    io.rdata(i) := data(io.raddr(i))
  }

  // below is the write ports (with priorities)
  for (i <- 0 until numWrite) {
    when (io.wen(i)) {
      data(io.waddr(i)) := io.wdata(i)
    }
  }

  // DataModuleTemplate should not be used when there're any write conflicts
  for (i <- 0 until numWrite) {
    for (j <- i+1 until numWrite) {
      assert(!(io.wen(i) && io.wen(j) && io.waddr(i) === io.waddr(j)))
    }
  }
}

class SyncDataModuleTemplate[T <: Data](gen: T, numEntries: Int, numRead: Int, numWrite: Int) extends Module {
  val io = IO(new Bundle {
    val raddr = Vec(numRead,  Input(UInt(log2Up(numEntries).W)))
    val rdata = Vec(numRead,  Output(gen))
    val wen   = Vec(numWrite, Input(Bool()))
    val waddr = Vec(numWrite, Input(UInt(log2Up(numEntries).W)))
    val wdata = Vec(numWrite, Input(gen))
  })

  val data = Mem(numEntries, gen)

  // read ports
  val raddr_reg = RegNext(io.raddr)
  for (i <- 0 until numRead) {
    io.rdata(i) := data(raddr_reg(i))
  }

  // below is the write ports (with priorities)
  for (i <- 0 until numWrite) {
    when (io.wen(i)) {
      data(io.waddr(i)) := io.wdata(i)
    }
  }

  // DataModuleTemplate should not be used when there're any write conflicts
  for (i <- 0 until numWrite) {
    for (j <- i+1 until numWrite) {
      assert(!(io.wen(i) && io.wen(j) && io.waddr(i) === io.waddr(j)))
    }
  }
}
