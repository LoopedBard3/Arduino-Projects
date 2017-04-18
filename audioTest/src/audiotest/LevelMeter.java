/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audiotest;

import javax.sound.sampled.*;




import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JComponent;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.border.EmptyBorder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;

import arduino.*;
import static java.lang.Thread.sleep;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner; 



public class LevelMeter extends JComponent {

    private int meterWidth = 10;

    private float amp = 0f;
    private float peak = 0f;

    public void setAmplitude(float amp) {
        this.amp = Math.abs(amp);
        repaint();
    }

    public void setPeak(float peak) {
        this.peak = Math.abs(peak);
        repaint();
    }

    public void setMeterWidth(int meterWidth) {
        this.meterWidth = meterWidth;
    }

    @Override
    protected void paintComponent(Graphics g) {
        int w = Math.min(meterWidth, getWidth());
        int h = getHeight();
        int x = getWidth() / 2 - w / 2;
        int y = 0;

        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(x, y, w, h);

        g.setColor(Color.BLACK);
        g.drawRect(x, y, w - 1, h - 1);

        int a = Math.round(amp * (h - 2));
        g.setColor(Color.GREEN);
        g.fillRect(x + 1, y + h - 1 - a, w - 2, a);

        int p = Math.round(peak * (h - 2));
        g.setColor(Color.RED);
        g.drawLine(x + 1, y + h - 1 - p, x + w - 1, y + h - 1 - p);
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension min = super.getMinimumSize();
        if (min.width < meterWidth) {
            min.width = meterWidth;
        }
        if (min.height < meterWidth) {
            min.height = meterWidth;
        }
        return min;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension pref = super.getPreferredSize();
        pref.width = meterWidth;
        return pref;
    }

    @Override
    public void setPreferredSize(Dimension pref) {
        super.setPreferredSize(pref);
        setMeterWidth(pref.width);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("Meter");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                JPanel content = new JPanel(new BorderLayout());
                content.setBorder(new EmptyBorder(25, 50, 25, 50));

                LevelMeter meter = new LevelMeter();
                meter.setPreferredSize(new Dimension(9, 100));
                content.add(meter, BorderLayout.CENTER);

                frame.setContentPane(content);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                new Thread(new Recorder(meter)).start();
            }
        });
    }

    static class Recorder implements Runnable {

        final LevelMeter meter;

        Recorder(final LevelMeter meter) {
            this.meter = meter;
        }

        @Override
        public void run() {
            
            
        Scanner scan = new Scanner(System.in);
        Arduino obj = new Arduino("COM3", 115200);
        obj.openConnection();
            try {
                sleep(4000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LevelMeter.class.getName()).log(Level.SEVERE, null, ex);
            }
        System.out.println(obj.getPortDescription());
        System.out.println(obj.serialRead());
        
                
        
        
        
        
        
            AudioFormat fmt = new AudioFormat(44100f, 16, 1, true, false);
            final int bufferByteSize = 2048;

            TargetDataLine line;
            try {
                line = AudioSystem.getTargetDataLine(fmt);
                line.open(fmt, bufferByteSize);
            } catch (LineUnavailableException e) {
                System.err.println(e);
                return;
            }

            byte[] buf = new byte[bufferByteSize];
            float[] samples = new float[bufferByteSize / 2];

            float lastPeak = 0f;

            line.start();
            for (int b; (b = line.read(buf, 0, buf.length)) > -1;) {

                // convert bytes to samples here
                for (int i = 0, s = 0; i < b;) {
                    int sample = 0;

                    sample |= buf[i++] & 0xFF; // (reverse these two lines
                    sample |= buf[i++] << 8;   //  if the format is big endian)

                // normalize to range of +/-1.0f
                    samples[s++] = sample / 32768f;

                }

                float rms = 0f;
                float peak = 0f;
                for (float sample : samples) {

                    float abs = Math.abs(sample);
                    if (abs > peak) {
                        peak = abs;
                    }

                    rms += sample * sample;
                }

                rms = (float) Math.sqrt(rms / samples.length);

                if (lastPeak > peak) {
                    peak = lastPeak * 0.99f;
                }

                lastPeak = peak;

                setMeterOnEDT(rms, peak);
                obj.serialWrite(String.valueOf(rms * 254) + "/n");
            try {
                sleep(20);
            } catch (InterruptedException ex) {
                Logger.getLogger(LevelMeter.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
            
        obj.closeConnection();
        }

        void setMeterOnEDT(final float rms, final float peak) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    meter.setAmplitude(rms);
                    meter.setPeak(peak);
                }
            });
        }
    }
}












/*import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class SoundMeter {

JFrame j;

public SoundMeter() {
j = new JFrame("SoundMeter");
j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
j.setLayout(new BoxLayout(j.getContentPane(), BoxLayout.Y_AXIS));
printMixersDetails();
j.setVisible(true);

}
public void printMixersDetails(){
javax.sound.sampled.Mixer.Info[] mixers = AudioSystem.getMixerInfo();
System.out.println("There are " + mixers.length + " mixer info objects");
for(int i=0;i<mixers.length;i++){
Mixer.Info mixerInfo = mixers[i];
System.out.println("Mixer Name:"+mixerInfo.getName());
Mixer mixer = AudioSystem.getMixer(mixerInfo);
Line.Info[] lineinfos = mixer.getTargetLineInfo();
for(Line.Info lineinfo : lineinfos){
System.out.println("line:" + lineinfo);
try {
Line line = mixer.getLine(lineinfo);
line.open();
if(line.isControlSupported(FloatControl.Type.VOLUME)){
FloatControl control = (FloatControl) line.getControl(FloatControl.Type.VOLUME);
System.out.println("Volume:"+control.getValue());
JProgressBar pb = new JProgressBar();
// if you want to set the value for the volume 0.5 will be 50%
// 0.0 being 0%
// 1.0 being 100%
//control.setValue((float) 0.5);
int value = (int) (control.getValue()*100);
pb.setValue(value);
j.add(new JLabel(lineinfo.toString()));
j.add(pb);
j.pack();
}
} catch (LineUnavailableException e) {
e.printStackTrace();
}
}
}
}
public static void main(String[] args) {
new SoundMeter();
}
}*/





/*import java.io.File;
import java.net.URL;
import javax.swing.*;
import javax.sound.sampled.*;

public class LoopSound {

public static void main(String[] args) throws Exception {
URL url = new File("C:\\Users\\pbibu\\Desktop\\ID_-_Believer_LNVS_Remix_.wav").toURI().toURL();
Clip clip = AudioSystem.getClip();
// getAudioInputStream() also accepts a File or InputStream
AudioInputStream ais = AudioSystem.
getAudioInputStream( url );
clip.open(ais);
clip.loop(Clip.LOOP_CONTINUOUSLY);
SwingUtilities.invokeLater(new Runnable() {
public void run() {
// A GUI element to prevent the Clip's daemon Thread
// from terminating at the end of the main()
System.out.println("running");
JOptionPane.showMessageDialog(null, "Close to exit!");
}
});
}
}*/
