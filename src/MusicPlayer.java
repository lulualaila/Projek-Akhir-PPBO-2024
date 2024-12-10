import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.*;
import java.util.ArrayList;

public class MusicPlayer extends PlaybackListener {
    private static final Object playSignal = new Object();

    private MusicPlayerGUI musicPlayerGUI;
    private Lagu laguSaatIni;
    public Lagu getLaguSaatIni(){
        return laguSaatIni;
    }

    private ArrayList<Lagu> playlist;
    private int indexPlaylistSaatIni;
    private AdvancedPlayer advancedPlayer;
    private boolean berhenti;
    private boolean laguSelesai;
    private boolean pencetNext, pencetPrev;
    private int frameSaatIni;
    
    public void setFrameSaatIni(int frame){
        frameSaatIni = frame;
    }

    private int waktuSaatIniDalamMili;
    public void setWaktuSaatIniDalamMili(int waktuDalamMili){
    	waktuSaatIniDalamMili = waktuDalamMili;
    }

    public MusicPlayer(MusicPlayerGUI musicPlayerGUI){
        this.musicPlayerGUI = musicPlayerGUI;
    }

    public void loadLagu(Lagu lagu){
        laguSaatIni = lagu;
        playlist = null;

        if(!laguSelesai)
            stopLagu();

        if(laguSaatIni != null){
            frameSaatIni = 0;
            waktuSaatIniDalamMili = 0;

            musicPlayerGUI.setNilaiPlaybackSlider(0);

            mainkanLaguSaatIni();
        }
    }

    public void loadPlaylist(File playlistFile){
        playlist = new ArrayList<>();

        try{
            FileReader fileReader = new FileReader(playlistFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String songPath;
            while((songPath = bufferedReader.readLine()) != null){
                Lagu song = new Lagu(songPath);
                playlist.add(song);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        if(playlist.size() > 0){
            musicPlayerGUI.setNilaiPlaybackSlider(0);
            waktuSaatIniDalamMili = 0;
            laguSaatIni = playlist.get(0);

            frameSaatIni = 0;
            musicPlayerGUI.aktifkanPauseButtonNonaktifkanPlayButton();
            musicPlayerGUI.updateJudulLaguDanArtisLagu(laguSaatIni);
            musicPlayerGUI.updatePlaybackSlider(laguSaatIni);
            mainkanLaguSaatIni();
        }
    }

    public void pauseLagu(){
        if(advancedPlayer != null){
            // update isPaused flag
            berhenti = true;

            // then we want to stop the player
            stopLagu();
        }
    }

    public void stopLagu(){
        if(advancedPlayer != null){
            advancedPlayer.stop();
            advancedPlayer.close();
            advancedPlayer = null;
        }
    }

    public void nextLagu(){
        if(playlist == null) return;
        if(indexPlaylistSaatIni + 1 > playlist.size() - 1) return;

        pencetNext = true;

        if(!laguSelesai)
            stopLagu();
        
        indexPlaylistSaatIni++;

        laguSaatIni = playlist.get(indexPlaylistSaatIni);

        frameSaatIni = 0;
        waktuSaatIniDalamMili = 0;

        musicPlayerGUI.aktifkanPauseButtonNonaktifkanPlayButton();
        musicPlayerGUI.updateJudulLaguDanArtisLagu(laguSaatIni);
        musicPlayerGUI.updatePlaybackSlider(laguSaatIni);

        // play the song
        mainkanLaguSaatIni();
    }

    public void laguSebelumnya(){
        if(playlist == null) return;

        if(indexPlaylistSaatIni - 1 < 0) return;

        pencetPrev = true;

        if(!laguSelesai)
            stopLagu();

        indexPlaylistSaatIni--;

        laguSaatIni = playlist.get(indexPlaylistSaatIni);

        frameSaatIni = 0;

        waktuSaatIniDalamMili = 0;

        musicPlayerGUI.aktifkanPauseButtonNonaktifkanPlayButton();
        musicPlayerGUI.updateJudulLaguDanArtisLagu(laguSaatIni);
        musicPlayerGUI.updatePlaybackSlider(laguSaatIni);

        mainkanLaguSaatIni();
    }

    public void mainkanLaguSaatIni(){
        if(laguSaatIni == null) return;

        try{
            FileInputStream fileInputStream = new FileInputStream(laguSaatIni.getFilePath());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            advancedPlayer = new AdvancedPlayer(bufferedInputStream);
            advancedPlayer.setPlayBackListener(this);

            mulaiMusicThread();

            mulaiPlaybackSliderThread();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void mulaiMusicThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    if(berhenti){
                        synchronized(playSignal){
                            berhenti = false;
                            playSignal.notify();
                        }

                        advancedPlayer.play(frameSaatIni, Integer.MAX_VALUE);
                    }else{
                        advancedPlayer.play();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void mulaiPlaybackSliderThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(berhenti){
                    try{
                        synchronized(playSignal){
                            playSignal.wait();
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                while(!berhenti && !laguSelesai && !pencetNext && !pencetPrev){
                    try{
                        waktuSaatIniDalamMili++;

                        int calculatedFrame = (int) ((double) waktuSaatIniDalamMili * 2.08 * laguSaatIni.getFrameRateDalamMilisekon());

                        musicPlayerGUI.setNilaiPlaybackSlider(calculatedFrame);

                        Thread.sleep(1);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void playbackStarted(PlaybackEvent evt) {
        System.out.println("Playback Mulai");
        laguSelesai = false;
        pencetNext = false;
        pencetPrev = false;
    }

    @Override
    public void playbackFinished(PlaybackEvent evt) {
        System.out.println("Playback Selesai");
        if(berhenti){
            frameSaatIni += (int) ((double) evt.getFrame() * laguSaatIni.getFrameRateDalamMilisekon());
        }else{
            if(pencetNext || pencetPrev) return;

            laguSelesai = true;

            if(playlist == null){
                musicPlayerGUI.aktifkanPlayButtonNonaktifkanPauseButton();
            }else{
                if(indexPlaylistSaatIni == playlist.size() - 1){
                    musicPlayerGUI.aktifkanPlayButtonNonaktifkanPauseButton();
                }else{
                    nextLagu();
                }
            }
        }
    }
}