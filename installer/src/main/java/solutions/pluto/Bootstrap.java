package solutions.pluto;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;

import javax.rmi.CORBA.Util;
import javax.swing.*;
import java.awt.*;

public class Bootstrap
{
    public static final String TITLE = "Future GOLD 2.13.5 Installer @ t.me/plutosolutions";

    private static InstallerFrame installer;

    public static void main( String[] args )
    {
        FlatLaf.registerCustomDefaultsSource( "assets" );
        FlatDarkLaf.setup();

        if ( !System.getProperty( "os.name" ).toLowerCase().startsWith( "win" ) )
        {
            Utils.showError( "Future GOLD crack only support Windows OS", true );
            return;
        }

        installer = new InstallerFrame();
        SwingUtilities.invokeLater( () ->
        {
            installer.setVisible( true );
        } );
    }
}
