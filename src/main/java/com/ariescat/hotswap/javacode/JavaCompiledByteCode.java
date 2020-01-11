package com.ariescat.hotswap.javacode;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * �Զ���һ������֮����ֽ������
 *
 * @author Ariescat
 * @version 2020/1/10 20:34
 */
public class JavaCompiledByteCode extends SimpleJavaFileObject {

    /**
     * �������ֽ���
     */
    private ByteArrayOutputStream compiledBytecode;

    /**
     * Instantiates a new java file object impl.
     */
    public JavaCompiledByteCode(URI uri, Kind kind) {
        super(uri, kind);
    }

    @Override
    public OutputStream openOutputStream() {
        return compiledBytecode = new ByteArrayOutputStream();
    }

    /**
     * ��ȡ����ɹ����ֽ���byte[]
     */
    public byte[] getByteCode() {
        return compiledBytecode.toByteArray();
    }
}
