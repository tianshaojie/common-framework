package io.github.jsbd.common.serialization.bytebean.codec;

public interface FieldCodecProvider {

  ByteFieldCodec getCodecOf(FieldCodecCategory type);

  ByteFieldCodec getCodecOf(Class<?> clazz);

}
