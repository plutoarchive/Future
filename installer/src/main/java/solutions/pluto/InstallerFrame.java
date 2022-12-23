package solutions.pluto;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

public class InstallerFrame extends JFrame
{
    public static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    public static final int HEIGHT = 300, WIDTH = 600;
    public static final Font FONT = new Font( "Verdana", 0, 15 );

    public InstallerFrame()
    {
        super( Bootstrap.TITLE );
        this.setBounds( SCREEN_SIZE.width / 2 - ( WIDTH / 2 ), SCREEN_SIZE.height / 2 - ( HEIGHT / 2 ), WIDTH, HEIGHT );
        this.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
        this.setLayout( null );
        this.setResizable( false );
        try
        {
            this.setIconImage( ImageIO.read( Objects.requireNonNull( getClass().getResourceAsStream( "/assets/logo.png" ) ) ) );
        } catch ( IOException e )
        {
            Utils.showError( "Logo not found", true );
            Runtime.getRuntime().halt( 1 );
        }

        JButton installButton = new JButton( "Install" );
        installButton.setBounds( 5, 215, 570, 30 );
        installButton.setFocusPainted( false );
        installButton.setFont( FONT );
        this.add( installButton );

        JLabel gamePathLabel = new JLabel( "Game path:" );
        gamePathLabel.setBounds( 5, 20, 100, 25 );
        gamePathLabel.setFont( FONT );
        this.add( gamePathLabel );

        JTextField gamePathField = new JTextField();
        gamePathField.setBounds( 5, 45, 540, 25 );
        gamePathField.setEditable( false );
        gamePathField.setText(
                Utils.getPath().exists() ? Utils.getPath().getAbsolutePath() : "Failed to detect your game path.. Choose a game directory yourself"
        );
        this.add( gamePathField );

        JButton selectPathBtn = new JButton( "..." );
        selectPathBtn.setBounds( 550, 45, 25, 25 );
        add( selectPathBtn );

        JLabel profileLabel = new JLabel( "Minecraft profile:" );
        profileLabel.setBounds( 5, 80, 170, 25 );
        profileLabel.setFont( FONT );
        this.add( profileLabel );

        JComboBox< String > mcProfile = new JComboBox<>( Utils.getMinecraftProfiles() );
        mcProfile.setBounds( 5, 105, 570, 25 );
        mcProfile.setEditable( false );
        mcProfile.grabFocus();
        this.add( mcProfile );

        JLabel baritoneLabel = new JLabel( "Additional (only with forge):" );
        baritoneLabel.setFont( FONT );
        baritoneLabel.setBounds( 5, 135, 250, 25 );
        this.add( baritoneLabel );

        JCheckBox baritoneCheckBox = new JCheckBox( "Install Baritone" );
        baritoneCheckBox.setBounds( 3, 160, 170, 25 );
        add( baritoneCheckBox );

        JCheckBox optifineCheckBox = new JCheckBox( "Install Optifine" );
        optifineCheckBox.setBounds( 3, 183, 170, 25 );
        add( optifineCheckBox );

        selectPathBtn.addActionListener( e ->
        {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
            if ( fileChooser.showOpenDialog( this ) == JFileChooser.APPROVE_OPTION )
            {
                final File selectedFile = fileChooser.getSelectedFile();
                if ( !( new File( selectedFile, "libraries" ).exists() &&
                        new File( selectedFile, "versions" ).exists() ) )
                {
                    Utils.showError( "This folder seems to be not minecraft directory", false );
                    return;
                }

                gamePathField.setText( selectedFile.getAbsolutePath() );
                Utils.setPath( selectedFile );
                mcProfile.removeAllItems();
                for ( String minecraftProfile : Utils.getMinecraftProfiles() )
                {
                    mcProfile.addItem( minecraftProfile );
                }
            }
        } );
        installButton.addActionListener( e ->
        {
            if ( mcProfile.getSelectedItem() == null || mcProfile.getSelectedItem().toString().isEmpty() )
            {
                Utils.showError( "Select the actual profile first", false );
                return;
            }
            File futureDir = new File( System.getProperty( "user.home" ) + "\\Future" );
            futureDir.mkdir();
            File authKey = new File( futureDir, "auth_key" );
            try
            {
                if ( !authKey.exists() && !authKey.createNewFile() ) throw new IOException();
                Files.write( authKey.toPath(), Utils.readAllBytes( Objects.requireNonNull( getClass().getResourceAsStream( "/assets/auth_key" ) ) ) );
            } catch ( Exception ex )
            {
                Utils.showError( "Failed to write the auth key", false );
                return;
            }
            JsonObject profiles = Utils.getProfiles();
            assert profiles != null;
            profiles.get( "profiles" ).getAsJsonObject().asMap().forEach( ( k, v ) ->
            {
                String name = v.getAsJsonObject().get( "name" ).getAsString();
                if ( !name.equals( mcProfile.getSelectedItem() ) )
                    return;
                File verFolder = new File( new File( Utils.getPath(), "versions" ),
                        v.getAsJsonObject().get( "lastVersionId" ).getAsString() );
                File json = new File( verFolder, verFolder.getName() + ".json" );
                JsonObject verConfig = null;

                try
                {
                    if ( !json.exists() )
                        if ( !json.createNewFile() ) throw new IOException();
                    verConfig = JsonParser.parseReader( new FileReader( json ) ).getAsJsonObject();
                } catch ( Exception ex )
                {
                    Utils.showError( "Failed to write a version json config", false );
                    return;
                }

                final String mcArgs = "minecraftArguments";
                String minecraftArguments = verConfig.get( mcArgs ).getAsString();
                minecraftArguments += " --tweakClass com.example.tweaker.ILOVEFUTURECLIENT";
                minecraftArguments += " --tweakClass net.futureclient.loader.launch.launchwrapper.LaunchWrapperEntryPoint";
                verConfig.remove( mcArgs );
                verConfig.addProperty( mcArgs, minecraftArguments );

                final String[] libs = { "net.futureclient:loader:1.0", "com.example:tweaker:1.0" };
                for ( String lib : libs )
                {
                    JsonObject object = new JsonObject();
                    object.addProperty( "name", lib );
                    verConfig.get( "libraries" ).getAsJsonArray().add( object );
                }
                try
                {
                    Files.write( json.toPath(), new GsonBuilder().setPrettyPrinting().create().toJson( verConfig ).getBytes( StandardCharsets.UTF_8 ) );
                } catch ( IOException ex )
                {
                    Utils.showError( "Failed to write the JSON config for a version", false );
                    return;
                }
                File libFolder = new File( Utils.getPath(), "libraries" );
                final String[] files = { "net/futureclient/loader-1.0.jar", "com/example/tweaker-1.0.jar" };
                for ( String file : files )
                {
                    File dir = new File( libFolder, file.replace( "-", "/" )
                            .replace( ":", "/" ).replace( ".jar", "" ) );
                    if ( !dir.exists() && !dir.mkdirs() )
                    {
                        Utils.showError( "Failed to create package directories", false );
                        return;
                    }
                    File f = new File( dir, file.substring( file.lastIndexOf( '/' ) + 1 ) );
                    if (f.exists())
                    {
                        Utils.showError( "It seems like you already have future installed", false );
                        return;
                    }
                    try
                    {
                        if ( !f.exists() && !f.createNewFile() ) throw new IOException();
                        Files.write( f.toPath(), Utils.readAllBytes( Objects.requireNonNull(
                                getClass().getResourceAsStream( "/jars/" + file.substring( file.lastIndexOf( '/' ) + 1 ) + "yeah" ) ) ) );
                    } catch ( Exception ex )
                    {
                        Utils.showError( "Failed to extract required libraries", false );
                        return;
                    }
                    if ( baritoneCheckBox.isSelected() || optifineCheckBox.isSelected() )
                    {
                        File mods = new File( Utils.getPath(), "mods" );
                        if ( !mods.exists() )
                            mods.mkdir();

                        if ( baritoneCheckBox.isSelected() )
                        {
                            try
                            {
                                Files.write( new File( mods, "optifine.jar" ).toPath(),
                                        Utils.readAllBytes( Objects.requireNonNull( getClass().getResourceAsStream( "/jars/optifine.jaryeah" ) ) ) );
                            } catch ( IOException ex )
                            {
                                Utils.showError( "Failed to write the optifine jar", false );
                                return;
                            }
                        }
                        if ( optifineCheckBox.isSelected() )
                        {
                            try
                            {
                                Files.write( new File( mods, "baritone.jar" ).toPath(),
                                        Utils.readAllBytes( Objects.requireNonNull( getClass().getResourceAsStream( "/jars/baritone.jaryeah" ) ) ) );
                            } catch ( IOException ex )
                            {
                                Utils.showError( "Failed to write the baritone jar", false );
                                return;
                            }
                        }
                    }
                }
                JOptionPane.showMessageDialog( this, "Successfully installed Future+ GOLD 2.13.5! Enjoy =D", Bootstrap.TITLE, JOptionPane.INFORMATION_MESSAGE );
            } );
        } );
    }
}
