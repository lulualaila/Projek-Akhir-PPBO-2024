import com.mpatric.mp3agic.Mp3File;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;

public class Lagu {
    private String judulLagu;
    private String artisLagu;
    private String panjangLagu;
    private String filePath;
    private Mp3File mp3File;
    private double frameRatePerMilisekon;

    public Lagu(String filePath){
        this.filePath = filePath;
        try{
            mp3File = new Mp3File(filePath);
            frameRatePerMilisekon = (double) mp3File.getFrameCount() / mp3File.getLengthInMilliseconds();
            panjangLagu = ubahKeFormatPanjangLagu();

            AudioFile audioFile = AudioFileIO.read(new File(filePath));

            Tag tag =  audioFile.getTag();
            if(tag != null){
                judulLagu = tag.getFirst(FieldKey.TITLE);
                artisLagu = tag.getFirst(FieldKey.ARTIST);
            }else{
                judulLagu = "N/A";
                artisLagu = "N/A";
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private String ubahKeFormatPanjangLagu(){
        long menit = mp3File.getLengthInSeconds() / 60;
        long detik = mp3File.getLengthInSeconds() % 60;
        String formatWaktu = String.format("%02d:%02d", menit, detik);

        return formatWaktu;
    }

    // getters
    public String getJudulLagu() {
        return judulLagu;
    }

    public String getArtisLagu() {
        return artisLagu;
    }

    public String getPanjangLagu() {
        return panjangLagu;
    }

    public String getFilePath() {
        return filePath;
    }

    public Mp3File getMp3File(){
    	return mp3File;
    }
    
    public double getFrameRateDalamMilisekon(){
    	return frameRatePerMilisekon;
    }
}
















