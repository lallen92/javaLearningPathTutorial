package com.liam.learning;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main
{
    public static void main(String[] args) throws InvalidPathException
    {
        boolean invalidPath = true;
        Scanner scanner = new Scanner(System.in);
        Path mp3Directory = null;
        while (invalidPath)
        {
            System.out.println("Please enter a path:");
            String directory = scanner.nextLine();
            mp3Directory = Paths.get(directory);
            if (!Files.exists(mp3Directory))
            {
                System.out.println("The specified directory does not exist : " + mp3Directory);
            } else
            {
                invalidPath = false;
            }
        }

        List<Path> mp3Paths = new ArrayList<>();
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(mp3Directory, "*.mp3"))
        {
            paths.forEach(p ->
            {
                System.out.println("Found : " + p.getFileName().toString());
                mp3Paths.add(p);
            });
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        List<Song> songs = mp3Paths.stream().map(path ->
        {
            try
            {
                Mp3File mp3file = new Mp3File(path);
                ID3v2 id3 = mp3file.getId3v2Tag();
                return new Song(id3.getArtist(), id3.getYear(), id3.getAlbum(), id3.getTitle());
            }
            catch (IOException | UnsupportedTagException | InvalidDataException e)
            {
                throw new IllegalStateException(e);
            }
        }).collect(Collectors.toList());

        try (Connection conn = DriverManager.getConnection("jdbc:h2:~/mydatabase;AUTO_SERVER=TRUE;INIT=runscript from './create.sql'"))
        {
            PreparedStatement st = conn.prepareStatement("insert into SONGS (artist, year, album, title) values (?, ?, ?, ?);");

            for (Song song : songs)
            {
                st.setString(1, song.getArtist());
                st.setString(2, song.getYear());
                st.setString(3, song.getAlbum());
                st.setString(4, song.getTitle());
                st.addBatch();
            }

            int[] updates = st.executeBatch();
            System.out.println("Inserted [=" + updates.length + "] records into the database");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

}

   /* public static ArrayList<Path> getfiles(Path pathToMp3) throws IOException
    {
        List<Path> mp3Paths = new ArrayList<>();
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(pathToMp3, "*.mp3"))
        {
            paths.forEach(p ->
            {
                System.out.println("Found : " + p.getFileName().toString());
                mp3Paths.add(p);
            });
        }
        List<Song> songs = mp3Paths.stream().map(path ->
        {
            try
            {
                Mp3File mp3file = new Mp3File(path);
                ID3v2 id3 = mp3file.getId3v2Tag();
                return new Song(id3.getArtist(), id3.getYear(), id3.getAlbum(), id3.getTitle());
            } catch (IOException | UnsupportedTagException | InvalidDataException e)
            {
                throw new IllegalStateException(e);
            }
        }).collect(Collectors.toList());
        return (ArrayList<Path>) mp3Paths;
    }





    public static Path getPath()
    {
        boolean invalidPath = true;
        Scanner scanner = new Scanner(System.in);
        Path mp3Directory = null;
        while (invalidPath)
        {
            System.out.println("Please enter a path:");
            String directory = scanner.nextLine();
            mp3Directory = Paths.get(directory);
            if (!Files.exists(mp3Directory))
            {
                System.out.println("The specified directory does not exist : " + mp3Directory);
            } else
            {
                invalidPath = false;
            }
        }
        return mp3Directory;
    }
}

*/