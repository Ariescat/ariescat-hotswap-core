package com.ariescat.hotswap.test;

import com.ariescat.hotswap.example.RedefineBean;
import com.sun.tools.attach.VirtualMachine;

import java.lang.management.ManagementFactory;

/**
 * -javaagent:libs\\hotswap-agent-1.0.jar �� premain ��ʽ����
 *
 * @author Ariescat
 * @version 2020/1/11 23:39
 */
public class TestInstrumentRedefineClass {

    public static void main(String[] args) {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        //����Ϊ�˷�����ԣ���ӡ��������id
        String pid = name.split("@")[0];
        System.out.println("����Id��" + pid);

        new Thread(() -> {
            RedefineBean bean = new RedefineBean();
            while (true) {
                try {
                    //�ȸ���ִ��֮���ٴ�ʹ�������
                    bean.print();

                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5500);

                    System.out.println("loadAgent...");

                    // �� agentmain ��ʽ����
                    // VirtualMachine��jdk��tool.jar����Ķ���������Ҫ��pom.xml�������jar
                    VirtualMachine vm = VirtualMachine.attach(pid);
                    // ���·��������ڱ��ȸ��ķ���ģ�Ҳ�������pid�ķ���Ҳ����ʹ�þ���·����
                    vm.loadAgent("libs\\hotswap-agent-1.0.jar");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
