import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;

public class MusicPlayerGUI extends JFrame {
    // color configurations
    public static final Color FRAME_COLOR = Color.BLACK;
    public static final Color TEXT_COLOR = Color.WHITE;

    private MusicPlayer musicPlayer;

    // allow us to use file explorer in our app
    private JFileChooser jFileChooser;

    private JLabel judulLagu, artisLagu;
    private JPanel playbackBtns;
    private JSlider playbackSlider;

    public MusicPlayerGUI(){
        // calls JFrame constructor to configure out gui and set the title heaader to "Music Player"
        super("Music Player");

        // set the width and height
        setSize(400, 600);

        // end process when app is closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // launch the app at the center of the screen
        setLocationRelativeTo(null);

        // prevent the app from being resized
        setResizable(false);

        // set layout to null which allows us to control the (x, y) coordinates of our components
        // and also set the height and width
        setLayout(null);

        // change the frame color
        getContentPane().setBackground(FRAME_COLOR);

        musicPlayer = new MusicPlayer(this);
        jFileChooser = new JFileChooser();

        // set a default path for file explorer
        jFileChooser.setCurrentDirectory(new File("src/assets"));

        // filter file chooser to only see .mp3 files
        jFileChooser.setFileFilter(new FileNameExtensionFilter("MP3", "mp3"));

        addGuiComponents();
    }

    private void addGuiComponents(){
        // add toolbar
        addToolbar();

        // load record image
        JLabel songImage = new JLabel(loadGambar("src/assets/record.png"));
        songImage.setBounds(0, 50, getWidth() - 20, 225);
        add(songImage);

        // song title
        judulLagu = new JLabel("Judul Lagu");
        judulLagu.setBounds(0, 285, getWidth() - 10, 30);
        judulLagu.setFont(new Font("Dialog", Font.BOLD, 24));
        judulLagu.setForeground(TEXT_COLOR);
        judulLagu.setHorizontalAlignment(SwingConstants.CENTER);
        add(judulLagu);

        // song artist
        artisLagu = new JLabel("Artis");
        artisLagu.setBounds(0, 315, getWidth() - 10, 30);
        artisLagu.setFont(new Font("Dialog", Font.PLAIN, 24));
        artisLagu.setForeground(TEXT_COLOR);
        artisLagu.setHorizontalAlignment(SwingConstants.CENTER);
        add(artisLagu);

        // playback slider
        playbackSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        playbackSlider.setBounds(getWidth()/2 - 300/2, 365, 300, 40);
        playbackSlider.setBackground(null);
        playbackSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // when the user is holding the tick we want to the pause the song
                musicPlayer.pauseLagu();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // when the user drops the tick
                JSlider source = (JSlider) e.getSource();

                // get the frame value from where the user wants to playback to
                int frame = source.getValue();

                // update the current frame in the music player to this frame
                musicPlayer.setFrameSaatIni(frame);

                // update current time in milli as well
                musicPlayer.setWaktuSaatIniDalamMili((int) (frame / (2.08 * musicPlayer.getLaguSaatIni().getFrameRateDalamMilisekon())));

                // resume the song
                musicPlayer.mainkanLaguSaatIni();

                // toggle on pause button and toggle off play button
                aktifkanPauseButtonNonaktifkanPlayButton();
            }
        });
        add(playbackSlider);

        // playback buttons (i.e. previous, play, next)
        addPlaybackBtns();
    }

    private void addToolbar(){
        JToolBar toolBar = new JToolBar();
        toolBar.setBounds(0, 0, getWidth(), 20);

        // prevent toolbar from being moved
        toolBar.setFloatable(false);

        // add drop down menu
        JMenuBar menuBar = new JMenuBar();
        toolBar.add(menuBar);

        // now we will add a song menu where we will place the loading song option
        JMenu menuLagu = new JMenu("Lagu");
        menuBar.add(menuLagu);

        // add the "load song" item in the songMenu
        JMenuItem loadLagu = new JMenuItem("Pilih Lagu");
        loadLagu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // an integer is returned to us to let us know what the user did
                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                // this means that we are also checking to see if the user pressed the "open" button
                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null){
                    // create a song obj based on selected file
                    Lagu lagu = new Lagu(selectedFile.getPath());

                    // load song in music player
                    musicPlayer.loadLagu(lagu);

                    // update song title and artist
                    updateJudulLaguDanArtisLagu(lagu);

                    // update playback slider
                    updatePlaybackSlider(lagu);

                    // toggle on pause button and toggle off play button
                    aktifkanPauseButtonNonaktifkanPlayButton();
                }
            }
        });
        menuLagu.add(loadLagu);

        // now we will add the playlist menu
        JMenu playlistMenu = new JMenu("Playlist");
        menuBar.add(playlistMenu);

        // then add the items to the playlist menu
        JMenuItem buatPlaylist = new JMenuItem("Buat Playlist");
        buatPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // load music playlist dialog
                new MusicPlaylistDialog(MusicPlayerGUI.this).setVisible(true);
            }
        });
        playlistMenu.add(buatPlaylist);

        JMenuItem loadPlaylist = new JMenuItem("Pilih Playlist");
        loadPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter(new FileNameExtensionFilter("Playlist", "txt"));
                jFileChooser.setCurrentDirectory(new File("src/assets"));

                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null){
                    // stop the music
                    musicPlayer.stopLagu();

                    // load playlist
                    musicPlayer.loadPlaylist(selectedFile);
                }
            }
        });
        playlistMenu.add(loadPlaylist);

        add(toolBar);
    }

    private void addPlaybackBtns(){
        playbackBtns = new JPanel();
        playbackBtns.setBounds(0, 435, getWidth() - 10, 80);
        playbackBtns.setBackground(null);

        // previous button
        JButton prevButton = new JButton(loadGambar("src/assets/previous.png"));
        prevButton.setBorderPainted(false);
        prevButton.setBackground(null);
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // go to the previous song
                musicPlayer.laguSebelumnya();
            }
        });
        playbackBtns.add(prevButton);

        // play button
        JButton playButton = new JButton(loadGambar("src/assets/play.png"));
        playButton.setBorderPainted(false);
        playButton.setBackground(null);
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // toggle off play button and toggle on pause button
                aktifkanPauseButtonNonaktifkanPlayButton();

                // play or resume song
                musicPlayer.mainkanLaguSaatIni();
            }
        });
        playbackBtns.add(playButton);

        // pause button
        JButton pauseButton = new JButton(loadGambar("src/assets/pause.png"));
        pauseButton.setBorderPainted(false);
        pauseButton.setBackground(null);
        pauseButton.setVisible(false);
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // toggle off pause button and toggle on play button
                aktifkanPlayButtonNonaktifkanPauseButton();

                // pause the song
                musicPlayer.pauseLagu();
            }
        });
        playbackBtns.add(pauseButton);

        // next button
        JButton nextButton = new JButton(loadGambar("src/assets/next.png"));
        nextButton.setBorderPainted(false);
        nextButton.setBackground(null);
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // go to the next song
                musicPlayer.nextLagu();
            }
        });
        playbackBtns.add(nextButton);

        add(playbackBtns);
    }

    // this will be used to update our slider from the music player class
    public void setNilaiPlaybackSlider(int frame){
        playbackSlider.setValue(frame);
    }

    public void updateJudulLaguDanArtisLagu(Lagu lagu){
        judulLagu.setText(lagu.getJudulLagu());
        
        // Periksa apakah artis lagu kosong atau null
        String artis = lagu.getArtisLagu();
        if (artis == null || artis.trim().isEmpty()) {
            artis = "Unknown Artist";
        }
        artisLagu.setText(artis);
    }

    public void updatePlaybackSlider(Lagu lagu) {
        // Update max count for slider
        playbackSlider.setMaximum(lagu.getMp3File().getFrameCount());

        // Buat label untuk panjang lagu saja
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();

        // Tampilkan durasi lagu di tengah slider
        JLabel labelDuration = new JLabel(lagu.getPanjangLagu());
        labelDuration.setFont(new Font("Dialog", Font.BOLD, 18));
        labelDuration.setForeground(TEXT_COLOR);

        int tengah = lagu.getMp3File().getFrameCount() / 2; // Posisi tengah slider
        labelTable.put(tengah, labelDuration);

        playbackSlider.setLabelTable(labelTable);
        playbackSlider.setPaintLabels(true);
    }


    public void aktifkanPauseButtonNonaktifkanPlayButton(){
        // retrieve reference to play button from playbackBtns panel
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);

        // turn off play button
        playButton.setVisible(false);
        playButton.setEnabled(false);

        // turn on pause button
        pauseButton.setVisible(true);
        pauseButton.setEnabled(true);
    }

    public void aktifkanPlayButtonNonaktifkanPauseButton(){
        // retrieve reference to play button from playbackBtns panel
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);

        // turn on play button
        playButton.setVisible(true);
        playButton.setEnabled(true);

        // turn off pause button
        pauseButton.setVisible(false);
        pauseButton.setEnabled(false);
    }

    private ImageIcon loadGambar(String imagePath){
        try{
            // read the image file from the given path
            BufferedImage image = ImageIO.read(new File(imagePath));

            // returns an image icon so that our component can render the image
            return new ImageIcon(image);
        }catch(Exception e){
            e.printStackTrace();
        }

        // could not find resource
        return null;
    }
}









