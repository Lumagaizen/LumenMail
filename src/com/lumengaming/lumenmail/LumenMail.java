package com.lumengaming.lumenmail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Taylor
 */
public class LumenMail extends JavaPlugin implements Listener, CommandExecutor
{

	private HashMap<String, ArrayList<String>> mail;
	private final File file = new File("LumenMail.bin");

	//<editor-fold defaultstate="collapsed" desc="constructor / initiation methods. ">
	@Override
	public void onEnable()
	{
		try
		{
			getCommand("lumenmail").setExecutor(this);
			getServer().getPluginManager().registerEvents(this, this);
			mail = load(file);
		}
		catch (ClassNotFoundException | IOException ex)
		{
			Logger.getLogger(LumenMail.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void onDisable()
	{
		HandlerList.unregisterAll((Plugin) this);
		save(file);
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="login event">
	@EventHandler(priority = EventPriority.HIGH)
	public void onLogin(PlayerJoinEvent e)
	{
		Player p = e.getPlayer();
		if (mail.containsKey(p.getName()))
		{
			if (!mail.get(p.getName()).isEmpty())
			{
				p.sendMessage("§2§k|||§2You have new mail. Use §a/mail get§2 to view it.§2§k|||");
				p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 1);
			}
		}
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="Save / Load">
	private void save(File file)
	{
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(this.mail);
			oos.flush();
			oos.close();
		}
		catch (Exception ex)
		{
			System.out.print(ex);
		}
	}

	private HashMap<String, ArrayList<String>> load(File file) throws
			ClassNotFoundException, IOException
	{
		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
			return (HashMap) ois.readObject();
		}
		catch (FileNotFoundException ex)
		{
			Bukkit.getLogger().info("Mail cache file not found!");
			return generate();
		}
		catch (IOException e)
		{
			Bukkit.getLogger().severe("IOException encountered loading mail!");
		}
		return generate();
	}

	private HashMap<String, ArrayList<String>> generate()
	{
		return new HashMap();
	}

	private ArrayList<String> getMail(String target)
	{
		return mail.containsKey(target) ? (ArrayList) this.mail.get(target) : null;
	}

	//</editor-fold>
	
	//<editor-fold defaultstate="collapsed" desc="send/get mail">
	@Override
	public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] args)
	{
		if (args.length == 1 && args[0].equalsIgnoreCase("get"))
		{
			if (mail.containsKey(cs.getName()))
			{
				cs.sendMessage("===== Mail: " + cs.getName() + " =====");
				ArrayList<String> msgs = mail.remove(cs.getName());
				for (String msg : msgs)
				{
					cs.sendMessage("§f" + msg);
				}
			}
			else
			{
				cs.sendMessage("You have no mail!");
			}
		}
		else if (args.length < 2)
		{
			cs.sendMessage("§c/mail [send] <name> <message>");
			cs.sendMessage("§c/mail get");
		}
		else
		{
			try
			{
				int startIndex = (args[0].equals("send")) ? 1 : 0;
				String message = cs.getName() + " :";
				for (int i = 1 + startIndex; i < args.length; i++)
				{
					message += " " + args[i];
				}
				cs.sendMessage(sendMail(args[startIndex], message));
			}
			catch (ArrayIndexOutOfBoundsException aie)
			{
				cs.sendMessage("§c/mail [send] <name> <message>");
				cs.sendMessage("§c/mail get");
			}

		}
		return false;
	}

	/**
	 * returns the status of the send *
	 */
	private String sendMail(String target, String message)
	{
		message = message.replace('&', '§');
		String status = "";
		Player player = getPlayer(target);
		OfflinePlayer op;
		if (player != null)
		{
			op = getOfflinePlayer(player.getName());
		}
		else
		{
			op = getOfflinePlayer(target);
		}
		if (op != null)
		{
			ArrayList userMail;
			if (mail.containsKey(op.getName()))
			{
				userMail = mail.remove(op.getName());//returns the value removedx
			}
			else
			{
				userMail = new ArrayList();
			}
			userMail.add(message);
			mail.put(op.getName(), userMail);
			Player p = op.getPlayer();
			if (p != null && p.isValid())
			{
				p.sendMessage("§2§k|||§2You have new mail. Use §a/mail get§2 to view it.§2§k|||");
				p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 1);
			}
			save(file);
			return "§2Sent message to " + op.getName() + ".";
		}
		return "§cNo user exists with the name or partial name : " + target + ".";
	}
//</editor-fold>

	/**
	 * returns null if display name or regular name are not found. *
	 */
	private OfflinePlayer getOfflinePlayer(String name)
	{
		name = name.toLowerCase();
		OfflinePlayer op = Bukkit.getOfflinePlayer(name);
		if (op != null && op.hasPlayedBefore())
		{
			return op;
		}
		OfflinePlayer[] list = Bukkit.getOfflinePlayers();
		for (int i = 0; i < list.length; i++)
		{
			op = list[i];
			if (op.getName().toLowerCase().startsWith(name))
			{
				return op;
			}
		}
		return null;
	}

	/**
	 * returns null if display name or regular name are not found. *
	 */
	public static Player getPlayer(String name)
	{
		name = name.toLowerCase();
		Player p = Bukkit.getPlayer(name);
		if (p != null && p.isOnline())
		{
			return p;
		}
		for (Player n : Bukkit.getOnlinePlayers())
		{
			String nick = ChatColor.stripColor(n.getDisplayName()).toLowerCase();
			if (nick.contains(name))
			{
				return n;
			}
		}
		return null;
	}
}
