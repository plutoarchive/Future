package solutions.pluto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class Utils
{
    public static String[] getMinecraftProfiles()
    {
        if ( !getPath().exists() )
            return new String[ 0 ];
        File profiles = new File( getPath(), "launcher_profiles.json" );
        JsonObject object = getProfiles();
        List< String > ret = new LinkedList<>();
        object.get( "profiles" ).getAsJsonObject().asMap().forEach( ( k, v ) ->
        {
            if ( k.equals( "settings" ) )
                return;
            String name = v.getAsJsonObject().get( "name" ).getAsString();
            if ( name.isEmpty() )
                return;
            ret.add( name );
        } );
        return ret.toArray( new String[ 0 ] );
    }

    private static File cached;

    public static File getPath()
    {
        if ( cached == null )
            cached = new File( String.format( "%s\\AppData\\Roaming\\.minecraft", System.getProperty( "user.home" ) ) );
        return cached;
    }

    public static void setPath( File file )
    {
        cached = file;
    }

    public static void showError( String text, boolean exit )
    {
        JOptionPane.showMessageDialog( null, text, Bootstrap.TITLE, JOptionPane.ERROR_MESSAGE );
        if ( exit )
            Runtime.getRuntime().halt( 1 );
    }

    public static JsonObject getProfiles()
    {
        File profiles = new File( getPath(), "launcher_profiles.json" );
        try
        {
            return JsonParser.parseReader( new FileReader( profiles ) ).getAsJsonObject();
        } catch ( FileNotFoundException e )
        {
            showError( "Error during reading launcher profiles\n" + e.getLocalizedMessage(), false );
            return null;
        }
    }

    public static byte[] readAllBytes( InputStream inputStream ) throws IOException
    {
        final int bufLen = 4 * 0x400; // 4KB
        byte[] buf = new byte[ bufLen ];
        int readLen;
        IOException exception = null;

        try
        {
            try ( ByteArrayOutputStream outputStream = new ByteArrayOutputStream() )
            {
                while ( ( readLen = inputStream.read( buf, 0, bufLen ) ) != -1 )
                    outputStream.write( buf, 0, readLen );

                return outputStream.toByteArray();
            }
        } catch ( IOException e )
        {
            exception = e;
            throw e;
        } finally
        {
            if ( exception == null ) inputStream.close();
            else try
            {
                inputStream.close();
            } catch ( IOException e )
            {
                exception.addSuppressed( e );
            }
        }
    }
}
