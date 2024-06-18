package com.example.shared_library

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> toByteArray(data: T): ByteArray {
    return ProtoBuf.encodeToByteArray(data)
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> fromByteArray(byteArray: ByteArray): T {
    return ProtoBuf.decodeFromByteArray(byteArray)
}


