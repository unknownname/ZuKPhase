package net.botwithus;

import jdk.jshell.JShell;
import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.events.EventBus;
import net.botwithus.rs3.events.Subscription;
import net.botwithus.rs3.events.impl.ChatMessageEvent;
import net.botwithus.rs3.events.impl.ServerTickedEvent;
import net.botwithus.rs3.events.impl.SkillUpdateEvent;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.Inventory;
import net.botwithus.rs3.game.actionbar.ActionBar;
import net.botwithus.rs3.game.hud.interfaces.Component;
import net.botwithus.rs3.game.hud.interfaces.Interfaces;
import net.botwithus.rs3.game.js5.types.configs.ConfigManager;
import net.botwithus.rs3.game.queries.builders.characters.NpcQuery;
import net.botwithus.rs3.game.queries.builders.components.ComponentQuery;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.scene.entities.characters.npc.Npc;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.game.vars.VarManager;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.TickingScript;
import net.botwithus.rs3.script.config.ScriptConfig;
import net.botwithus.rs3.util.Regex;

import java.sql.ResultSet;
import java.time.Instant;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZukPhaseTick extends TickingScript {

    private ZukPhaseTick.BotState botState = ZukPhaseTick.BotState.IDLE;
    String message ="";
    String matchtext = "";
    boolean success = false;
    private Random random = new Random();
    private Pattern restore = Regex.getPatternForContainsString("Super restore potion");
    enum BotState {
        //define your own states here
        IDLE,
        SKILLING,
        BANKING,
        //...
    }
    public ZukPhaseTick(String s, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(s, scriptConfig, scriptDefinition);
        this.sgc = new ZukPhaseGraphicsContext(getConsole(), this);

        subscribe(ChatMessageEvent.class, chatMessageEvent -> {
            message = chatMessageEvent.getMessage();
            String regex = "</col=[A-Fa-f0-9]+>(.*?)</col>"; //chatMessageEvent.getMessage();
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(message);


            if(matcher.find())
            {
                matchtext = matcher.group(1);
                println("Message in the chat" + matchtext);
            }


        });

        subscribe(SkillUpdateEvent.class, skillUpdateEvent -> {
            if(skillUpdateEvent.getId() == Skills.CONSTRUCTION.getId())
            {
                if(skillUpdateEvent.getCurrentLevel() < skillUpdateEvent.getActualLevel())
                {
                    ActionBar.useItem(String.valueOf(restore), "Drink");
                }
            }

        });


    }


 /*  public boolean onInitialize()
    {
        super.onInitialize();
        setActive(false);


        return true;
    }*/

    @Override
    public void onTick(LocalPlayer localPlayer) {
        LocalPlayer player = Client.getLocalPlayer();
        if (player == null || Client.getGameState() != Client.GameState.LOGGED_IN || botState == ZukPhaseTick.BotState.IDLE) {
            //wait some time so we dont immediately start on login.
            Execution.delay(random.nextLong(3000, 7000));
            return;
        }
        switch (botState) {
            case IDLE -> {
                //do nothing
                println("We're idle!");
                Execution.delay(random.nextLong(1000, 3000));
            }
            case SKILLING -> {
                //do some code that handles your skilling
                Execution.delay(handleboss(player));

            }
            case BANKING -> {
                //handle your banking logic, etc
            }
        }
    }

    private long handleboss(LocalPlayer player)
    {
        Npc ZukBoss = NpcQuery.newQuery().name("TzKal-Zuk").results().first();
        if (ZukBoss == null) {
            return random.nextLong(750, 1500);
        }
        if(ZukBoss != null) {
            //println("Boss Animation: " + ZukBoss.getAnimationId());
            Component pain = ComponentQuery.newQuery(291).spriteId(30721).results().first();
            Component burn = ComponentQuery.newQuery(291).spriteId(30096).results().first();
            Component test = ComponentQuery.newQuery(291).results().first();
            /*println("Pain Debuff" + pain.getText());
            println("burn" + burn.getText());
            println("Test" + test.getSpriteId());*/



            if (burn != null) {
                println("Using Freedom");
                ActionBar.useAbility("Freedom");
                Processing_Melee();
            }
            if (ZukBoss.getAnimationId() == 34498) {
                //Melee Prayer
                Processing_Melee();

            } else if (ZukBoss.getAnimationId() == 34498) {
                //Melee
                Processing_Melee();
            } else if (ZukBoss.getAnimationId() == 34499) {
                //Mage Prayer
                Processing_Mage();
            } else if (ZukBoss.getAnimationId() == 34496) {
                //Melee
                Processing_Melee();
            }

            if(message.contains("Sear!") || message.contains("Suffer!"))
            {
                Processing_Mage_Melee_Switch();
                //println("Mage_Melee for Sear");
                println("Sear Mechanic");

            }
             else if(message.contains("You will break beneath me!"))
            {
                Processing_Geo();
                //println(" Freedom and swap Melee for Sear");
                println("Geothermal Burn");
            }else if(message.contains("Tremble before me!") || message.contains("Fall before my might!") || message.contains("The earth yields to me!"))
            {
                Processing_Mage_Melee_Switch();
                println("Quake Started");
            }
            else if(message.contains(" Die!") || message.contains(" Begone!") || message.contains(" Ful's flame burns within me!"))
            {
                EmpowerMagic();
                println("Empowered Magic");
            }
        }

        return random.nextLong(250,500);
    }

    private void Processing_Melee()
    {

        //ActionBar.usePrayer("Deflect Melee");
        println("Detecting Melee switch, attempting...");
        if(VarManager.getVarbitValue(16770) == 0)
            success = ActionBar.usePrayer("Deflect Magic");
        else {
            println("Varbit 16798 was" + VarManager.getVarbitValue(16770));
        }
    }

    private void Processing_Mage()
    {
        //ActionBar.usePrayer("Deflect Magic");

        println("Detecting Magic switch, attempting...");
        if(VarManager.getVarbitValue(16768) == 0)
            success = ActionBar.usePrayer("Deflect Magic");
        else {
            println("Varbit 16798 was" + VarManager.getVarbitValue(16768));
        }
    }

    private void Processing_Mage_Melee_Switch()
    {
        Processing_Mage();
        //ActionBar.useAbility("Deflect Mage");//
        ActionBar.useAbility("Surge");
        Processing_Melee();
        ActionBar.useItem(String.valueOf(restore), "Drink");
    }

    private void Processing_Geo()
    {
        ActionBar.useAbility("Freedom");
        Processing_Mage();
    }

    private void EmpowerMagic()
    {
        ActionBar.useAbility("Resonance");
    }

    public ZukPhaseTick.BotState getBotState() {
        return botState;
    }

    public void setBotState(ZukPhaseTick.BotState botState) {
        this.botState = botState;
    }

}