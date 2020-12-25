package sample;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.scene.control.Label;
import javafx.scene.media.AudioEqualizer;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class Controller {

    public static Controller controller;
    public static Controller getController() {
        return controller;
    }
    public static void setController(Controller controller) {
        Controller.controller = controller;
    }
    public void initialize(){
        Controller.setController(this);
        controller.set_playlist();
    }
    private String path;
    private MediaPlayer mediaPlayer;
    private MediaPlayer _mediaPlayer;

    @FXML
    private MediaView mediaView;

    @FXML
    private Slider progressBar;

    @FXML
    private VBox vBox;

    @FXML
    private Slider volumeSlider;

    @FXML
    private Label song_name;

    @FXML
    private TableView table;
    //String playlist_file = "C:\\Users\\mrnos\\Desktop\\playlist.txt";
    FileWriter playlist_file_writer;
    FileReader playlist_file_reader = new FileReader("C:\\Users\\mrnos\\Desktop\\playlist.txt");

    @FXML
    private TableColumn<String,String> name;

    @FXML
    private TableColumn<String, Integer> duration;

    static public ObservableList<File> selected_files;

    @FXML
    private ListView<File> list_of_songs;

    public Controller() throws IOException {
    }

    public void http(String song_full_name,String artist, String album,String query) {
        table = new TableView();
        Formatter f = new Formatter();
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(query).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            StringBuilder sb = new StringBuilder();

            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
                BufferedReader in = new BufferedReader((new InputStreamReader(connection.getInputStream())));
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                System.out.println(sb.toString());
                if (!sb.toString().split(",")[0].equals("{\"error\":6")) {
                    JsonObject jsonObject = new JsonParser().parse(sb.toString()).getAsJsonObject();
                    JsonElement duration_ = jsonObject.getAsJsonObject("album").getAsJsonObject("tracks").getAsJsonArray("track").get(0).getAsJsonObject().get("duration");
                    JsonElement name_ = jsonObject.getAsJsonObject("album").get("name");
                    System.out.println(name_);
                    Integer duration_min=duration_.getAsInt()/60 ;
                    Integer duration_sec=duration_.getAsInt()-duration_min*60;
                    duration.setCellValueFactory(new PropertyValueFactory<String,Integer>(f.format("%d : %d",duration_min,duration_sec).toString()));
                    name.setCellValueFactory(new PropertyValueFactory<String,String>(f.format("%s",name_.getAsString()).toString()));
                    table.getColumns().addAll(name,duration);


                }


            } else {
                System.out.println("Error " + connection.getResponseCode() + "," + connection.getResponseMessage());
            }
        } catch (Throwable cause) {
            cause.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public void set_playlist()  {
        try {
            ObservableList<File> list = FXCollections.observableArrayList();
            BufferedReader reader = new BufferedReader(playlist_file_reader);
            String line =reader.readLine();
            StringBuilder sb = new StringBuilder();
            sb.append(line);
            int line_ = sb.toString().split(",").length;
            for (int i = 0; i< line_;++i){
                File a = new File(sb.toString().split(",")[i].replaceAll("\\[","").replaceAll("]",""));
                list.add(a);
            }
            list_of_songs.setItems(list);
            path =  list.get(0).toURI().toString();
            System.out.println(path);
            Media media = new Media(path);
            mediaPlayer = new MediaPlayer(media);
            song_name.setText(path.split("/")[path.split("/").length - 1].replaceAll("%20", " "));

            mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
                @Override
                public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                    progressBar.setValue(newValue.toSeconds());

                }
            });

            progressBar.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    mediaPlayer.seek(Duration.seconds(progressBar.getValue()));
                }
            });

            progressBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    mediaPlayer.seek(Duration.seconds(progressBar.getValue()));
                }
            });

            mediaPlayer.setOnReady(new Runnable() {
                @Override
                public void run() {
                    Duration time = media.getDuration();
                    progressBar.setMax(time.toSeconds());
                }
            });
            volumeSlider.setValue(mediaPlayer.getVolume() * 100);
            volumeSlider.valueProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    mediaPlayer.setVolume(volumeSlider.getValue() / 100);
                }
            });
        }catch (Throwable cause){
            cause.printStackTrace();
            System.out.println("tyt gg");
        }


            }



    public void choose_method(javafx.event.ActionEvent event) throws IOException {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        ObservableList<File> list = FXCollections.observableArrayList();
        FileChooser fileChooser = new FileChooser();
        list.addAll( fileChooser.showOpenMultipleDialog(null));
        list_of_songs.setItems(list);
        path = list.get(0).toURI().toString();
        System.out.println(list.get(0));
        System.out.println(path);
        playlist_file_writer= new FileWriter("C:\\Users\\mrnos\\Desktop\\playlist.txt",false);
        playlist_file_writer.write(list.toString());
        playlist_file_writer.close();


        if (list_of_songs != null) {
            Media media = new Media(path);
            mediaPlayer = new MediaPlayer(media);
            song_name.setText(path.split("/")[path.split("/").length - 1].replaceAll("%20", " "));


            mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
                @Override
                public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                    progressBar.setValue(newValue.toSeconds());

                }
            });

            progressBar.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    mediaPlayer.seek(Duration.seconds(progressBar.getValue()));
                }
            });

            progressBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    mediaPlayer.seek(Duration.seconds(progressBar.getValue()));
                }
            });

            mediaPlayer.setOnReady(new Runnable() {
                @Override
                public void run() {
                    Duration time = media.getDuration();
                    progressBar.setMax(time.toSeconds());
                }
            });
            volumeSlider.setValue(mediaPlayer.getVolume() * 100);
            volumeSlider.valueProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    mediaPlayer.setVolume(volumeSlider.getValue() / 100);
                }
            });

        }
    }

    public void set_song(){
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        path = list_of_songs.getSelectionModel().getSelectedItem().toURI().toString().replaceAll("C:/Users/mrnos/IdeaProjects/mp3_player/%20","");
        System.out.println(path);
        Media media = new Media(path);
        mediaPlayer = new MediaPlayer(media);
        song_name.setText(path.split("/")[path.split("/").length - 1].replaceAll("%20", " "));
        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                progressBar.setValue(newValue.toSeconds());

            }
        });

        progressBar.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mediaPlayer.seek(Duration.seconds(progressBar.getValue()));
            }
        });

        progressBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mediaPlayer.seek(Duration.seconds(progressBar.getValue()));
            }
        });

        mediaPlayer.setOnReady(new Runnable() {
            @Override
            public void run() {
                Duration time = media.getDuration();
                progressBar.setMax(time.toSeconds());
            }
        });
        volumeSlider.setValue(mediaPlayer.getVolume() * 100);
        volumeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                mediaPlayer.setVolume(volumeSlider.getValue() / 100);
            }
        });
        String song_full_name = path.split("/")[path.split("/").length - 1].replaceAll("%20", " ");
        String artist = song_full_name.split(" -")[0].replaceAll(" ", "%20");
        String album = song_full_name.split("-")[1].split("\\.")[0].replaceFirst(" ", "").replaceAll(" ", "%20");
        String query = "https://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=81133b7adcb966c38b5f3bee1065d54e&artist=" + artist + "&album=" + album + "&format=json";
        http(song_full_name, artist, album,query);

    }


    public void play(javafx.event.ActionEvent event) {
        if (_mediaPlayer != null) {
            mediaPlayer.stop();
            _mediaPlayer = mediaPlayer;
        }
        mediaPlayer.play();


        progressBar.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediaPlayer.seek(Duration.seconds(progressBar.getValue()));
            }
        });

        progressBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediaPlayer.seek(Duration.seconds(progressBar.getValue()));
            }
        });

        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration oldValue, Duration newValue) {
                progressBar.setValue(mediaPlayer.getCurrentTime().toSeconds());
            }
        });
    }

    public void pause(javafx.event.ActionEvent event) {
        mediaPlayer.pause();
    }

    public void stop(javafx.event.ActionEvent event) {
        mediaPlayer.stop();
    }

    public void slow(javafx.event.ActionEvent event) {
        mediaPlayer.setRate(0.5);
    }

    public void fast(javafx.event.ActionEvent event) {
        mediaPlayer.setRate(2);
    }


}