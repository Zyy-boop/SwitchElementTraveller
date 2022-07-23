package test.gc.switchele.commands;

import emu.grasscutter.GameConstants;
import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.AvatarSkillDepotData;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.server.packet.send.PacketSceneEntityAppearNotify;
import test.gc.switchele.Switchele;

import java.util.List;

@Command(label = "switchelement", description = "Switch element for traveller", usage = "switchelement [White/Anemo/Geo/Electro/Dendro]", aliases = {"se"}, threading = true)
public class SwitchElement implements CommandHandler {

    private Element getElementFromString(String elementString) {
        return switch (elementString.toLowerCase()) {
            case "white", "common" -> Element.COMMON;
            case "fire", "pyro" -> Element.FIRE;
            case "water", "hydro" -> Element.WATER;
            case "wind", "anemo" -> Element.WIND;
            case "ice", "cryo" -> Element.ICE;
            case "rock", "geo" -> Element.ROCK;
            case "electro" -> Element.ELECTRO;
            case "grass", "dendro", "plant" -> Element.GRASS;
            default -> null;
        };
    }

    private boolean changeAvatarElement(Player sender, int avatarId, Element element) {
        Avatar avatar = sender.getAvatars().getAvatarById(avatarId);
        AvatarSkillDepotData skillDepot = GameData.getAvatarSkillDepotDataMap().get(element.getSkillRepoId(avatarId));
        if (avatar == null || skillDepot == null) {
            return false;
        }
        avatar.setSkillDepotData(skillDepot);
        avatar.setCurrentEnergy(1000);
        avatar.save();
        return true;
    }

    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        if (args.size() != 1) {
            CommandHandler.sendMessage(sender, "Usage: /se OR /switchelement [White/Anemo/Geo/Electro/Dendro]");
            return;
        }
        if (sender == null) {
            Switchele.getInstance().getLogger().info("SwitchElement command couldn't be called by console.");
            return;
        }

        Element element = getElementFromString(args.get(0));


        if (element == null) {
            CommandHandler.sendMessage(sender, "Invalid element");
            return;
        }

        boolean maleSuccess = changeAvatarElement(sender, GameConstants.MAIN_CHARACTER_MALE, element);
        boolean femaleSuccess = changeAvatarElement(sender, GameConstants.MAIN_CHARACTER_FEMALE, element);
        if (maleSuccess || femaleSuccess) {
            String success = String.format("Successfully changed element to %s", element.name());
            CommandHandler.sendMessage(sender, success);
            int scene = sender.getSceneId();
            sender.getWorld().transferPlayerToScene(sender, 1, sender.getPos());
            sender.getWorld().transferPlayerToScene(sender, scene, sender.getPos());
            sender.getScene().broadcastPacket(new PacketSceneEntityAppearNotify(sender));
        } else {
            CommandHandler.sendTranslatedMessage(sender, "Failed to change the Element.");
        }
    }
}