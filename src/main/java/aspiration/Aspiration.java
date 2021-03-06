package aspiration;

import aspiration.events.CultistTraining;
import aspiration.events.ElementalEggBirdNest;
import aspiration.events.TheDarkMirror;
import aspiration.relics.*;
import aspiration.relics.crossovers.EmptySkull;
import aspiration.relics.skillbooks.*;
import aspiration.relics.abstracts.AspirationRelic;
import basemod.BaseMod;
import basemod.ModLabeledToggleButton;
import basemod.ModPanel;
import basemod.ReflectionHacks;
import basemod.helpers.RelicType;
import basemod.interfaces.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.audio.Sfx;
import com.megacrit.cardcrawl.audio.SoundMaster;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.Exordium;
import com.megacrit.cardcrawl.dungeons.TheCity;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

@SpireInitializer
public class Aspiration implements
        PostInitializeSubscriber,
        EditStringsSubscriber,
        EditRelicsSubscriber,
        PostPowerApplySubscriber,
        PostDungeonInitializeSubscriber
{
	public static final Logger logger = LogManager.getLogger(Aspiration.class.getName());
    private static SpireConfig modConfig = null;
    public static SpireConfig otherSaveData = null;
    public static TextureAtlas powerAtlas;
    public static final int SKILLBOOK_SPAWN_AMOUNT = 4;

    // Crossover checks
    public static final boolean hasMarisa;
    public static final boolean hasServant;
    public static final boolean hasBeaked;

    static {
        hasMarisa = Loader.isModLoaded("TS05_Marisa");
        if (hasMarisa) {
            logger.info("Detected Character: Marisa");
        }
        hasServant = Loader.isModLoaded("BlackRuseMod");
        if (hasMarisa) {
            logger.info("Detected Character: Servant");
        }
        hasBeaked = Loader.isModLoaded("beakedthecultist-sts");
        if (hasMarisa) {
            logger.info("Detected Character: Beaked");
        }
    }

    public static void initialize()
    {
        BaseMod.subscribe(new Aspiration());

        try {
            Properties defaults = new Properties();
            defaults.put("WeakPoetsPen", Boolean.toString(true));
            defaults.put("uncommonNostalgia", Boolean.toString(false));
            defaults.put("SkillbookCardpool", Boolean.toString(true));
            modConfig = new SpireConfig("Aspiration", "Config", defaults);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String assetPath(String path)
    {
        return "aspirationAssets/" + path;
    }

    public static boolean weakPoetsPenEnabled()
    {
        if (modConfig == null) {
            return false;
        }
        return modConfig.getBool("WeakPoetsPen");
    }
    
    public static boolean uncommonNostalgia()
    {
        if (modConfig == null) {
            return false;
        }
        return modConfig.getBool("uncommonNostalgia");
    }

    public static boolean skillbookCardpool()
    {
        if (modConfig == null) {
            return false;
        }
        return modConfig.getBool("SkillbookCardpool");
    }
    
    public static void loadOtherData()
    {
        logger.info("Loading Other Save Data");
        try {
            otherSaveData = new SpireConfig("Aspiration", "OtherSaveData");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveData()
    {
        logger.info("Saving Data");
        try {
            if (otherSaveData == null) {
                otherSaveData = new SpireConfig("Aspiration", "OtherSaveData");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clearData()
    {
        logger.info("Clearing Saved Data");
        try {
            SpireConfig config = new SpireConfig("Aspiration", "SaveData");
            config.clear();
            config.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void receivePostInitialize() {
    	loadOtherData();

        UIStrings buttonStrings = CardCrawlGame.languagePack.getUIString("aspiration:ModButtonText");
        String[] TEXT = buttonStrings.TEXT;

        ModPanel settingsPanel = new ModPanel();
        ModLabeledToggleButton PPBtn = new ModLabeledToggleButton(TEXT[0], 350, 700, Settings.CREAM_COLOR, FontHelper.charDescFont, weakPoetsPenEnabled(), settingsPanel, l -> {},
                button ->
                {
                    if (modConfig != null) {
                        modConfig.setBool("WeakPoetsPen", button.enabled);
                        try {
                            modConfig.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        settingsPanel.addUIElement(PPBtn);
        
        ModLabeledToggleButton nostalgiaBtn = new ModLabeledToggleButton(TEXT[1], 350, 650, Settings.CREAM_COLOR, FontHelper.charDescFont, uncommonNostalgia(), settingsPanel, l -> {},
                button ->
                {
                    if (modConfig != null) {
                        modConfig.setBool("uncommonNostalgia", button.enabled);
                        try {
                            modConfig.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        settingsPanel.addUIElement(nostalgiaBtn);

        ModLabeledToggleButton skillbookBtn = new ModLabeledToggleButton(TEXT[2], 350, 600, Settings.CREAM_COLOR, FontHelper.charDescFont, skillbookCardpool(), settingsPanel, l -> {},
                button ->
                {
                    if (modConfig != null) {
                        modConfig.setBool("SkillbookCardpool", button.enabled);
                        try {
                            modConfig.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        settingsPanel.addUIElement(skillbookBtn);

        BaseMod.registerModBadge(ImageMaster.loadImage(assetPath("img/UI/modBadge.png")), "Aspiration", "Erasels", "A mod, boyo.", settingsPanel);

    	
        BaseMod.addEvent(TheDarkMirror.ID, TheDarkMirror.class);
        BaseMod.addEvent(ElementalEggBirdNest.ID, ElementalEggBirdNest.class, Exordium.ID);
        BaseMod.addEvent(CultistTraining.ID, CultistTraining.class, TheCity.ID);
        
        this.loadAudio();
        powerAtlas = new TextureAtlas(Gdx.files.internal(assetPath("img/powers/powers.atlas")));
    }

    @SuppressWarnings("unchecked")
	public void loadAudio() {
		HashMap<String, Sfx> map = (HashMap<String, Sfx>)ReflectionHacks.getPrivate(CardCrawlGame.sound, SoundMaster.class, "map");
        map.put("ASP-BLOODPUMP", new Sfx(assetPath("audio/BloodPump.ogg"), false));
    }
    
    @Override
    public void receiveEditRelics()
    {
    	//"Normal" Relics
    	BaseMod.addRelic(new HummingbirdHeart(), RelicType.SHARED);
    	BaseMod.addRelic(new Legacy_Headhunter(), RelicType.SHARED);
    	BaseMod.addRelic(new Headhunter(), RelicType.SHARED);
    	BaseMod.addRelic(new AnachronicSnailShell(), RelicType.SHARED);
    	BaseMod.addRelic(new SupercapacitiveCoin(), RelicType.SHARED);
    	BaseMod.addRelic(new PoetsPen(), RelicType.SHARED);
    	BaseMod.addRelic(new PoetsPen_weak(), RelicType.SHARED);
    	BaseMod.addRelic(new FetidBarrel(), RelicType.SHARED);
    	BaseMod.addRelic(new StickyExplosives(), RelicType.SHARED);
    	BaseMod.addRelic(new FrozenJewel(), RelicType.SHARED);
    	BaseMod.addRelic(new EvolvingReagent(), RelicType.SHARED);
    	BaseMod.addRelic(new Lifesprig(), RelicType.SHARED);
    	BaseMod.addRelic(new RitualDagger(), RelicType.SHARED);
    	BaseMod.addRelic(new KaomsHeart(), RelicType.SHARED);
    	BaseMod.addRelic(new Nostalgia(uncommonNostalgia()), RelicType.SHARED);
        BaseMod.addRelic(new TrainingWeights(), RelicType.SHARED);
        BaseMod.addRelic(new SeaSaltIceCream(), RelicType.SHARED);
        BaseMod.addRelic(new FutureDiary(), RelicType.SHARED);

        //Vanilla skillbooks
        BaseMod.addRelic(new IroncladSkillbook(), RelicType.SHARED);
        BaseMod.addRelic(new DefectSkillbook(), RelicType.SHARED);
        BaseMod.addRelic(new SilentSkillbook(), RelicType.SHARED);
    	
    	//Special relics
    	BaseMod.addRelic(new BabyByrd(), RelicType.SHARED);
    	BaseMod.addRelic(new RitualStick(), RelicType.SHARED);
        BaseMod.addRelic(new ArtOfWarUpgrade(), RelicType.SHARED);
    	
    	//Starter Upgrades
    	BaseMod.addRelic(new RingOfOuroboros(), RelicType.SHARED);
    	BaseMod.addRelic(new InfernalBlood(), RelicType.SHARED);
    	BaseMod.addRelic(new BursterCore(), RelicType.SHARED);
    	
    	//Defect Only
    	BaseMod.addRelic(new EnhancedActuators(), RelicType.BLUE);
    	BaseMod.addRelic(new MysteriousAuxiliaryCore(), RelicType.BLUE);
    	
    	//If poison card in deck
        BaseMod.addRelic(new VileToxins(), RelicType.SHARED);
        BaseMod.addRelic(new Contagion(), RelicType.SHARED);
        BaseMod.addRelic(new SneckoTail(), RelicType.SHARED);

        //Crossover
        if(hasMarisa) {
            BaseMod.addRelic(new MarisaSkillbook(), RelicType.SHARED);
        }
        if(hasServant) {
            BaseMod.addRelic(new ServantSkillbook(), RelicType.SHARED);
        }
        if(hasBeaked) {
            BaseMod.addRelic(new EmptySkull(), RelicType.SHARED);
            BaseMod.addRelic(new BeakedSkillbook(), RelicType.SHARED);
        }
    }

    private String languageSupport()
    {
        switch (Settings.language) {
            case RUS:
                return "rus";
            case DEU:
                return "deu";
            default:
                return "eng";
        }
    }

    private void loadLocStrings(String language)
    {
        String path = "loc/" + language + "/";

        BaseMod.loadCustomStringsFile(EventStrings.class, assetPath(path + "aspiration-EventStrings.json"));
        BaseMod.loadCustomStringsFile(UIStrings.class, assetPath(path + "aspiration-UIStrings.json"));
        BaseMod.loadCustomStringsFile(PowerStrings.class, assetPath(path + "aspiration-PowerStrings.json"));
        BaseMod.loadCustomStringsFile(RelicStrings.class, assetPath(path + "aspiration-RelicStrings.json"));
    }

    @Override
    public void receiveEditStrings()
    {
        String language = languageSupport();

        loadLocStrings("eng");
        loadLocStrings(language);
    }

    @Override
    public void receivePostDungeonInitialize()
    {
        if (weakPoetsPenEnabled()) {
            if (AbstractDungeon.bossRelicPool.removeIf(r -> r.equals(PoetsPen.ID))) {
                logger.info(PoetsPen.ID + " removed.");
            }
        } else {
        	if (AbstractDungeon.bossRelicPool.removeIf(r -> r.equals(PoetsPen_weak.ID))) {
                logger.info(PoetsPen_weak.ID + " removed.");
            }
        }

        //Allow only SKILLBOOK_SPAWN_AMOUNT skillbooks into the boss relic pool
        Random rng = AbstractDungeon.relicRng;
        ArrayList<SkillbookRelic> skillbookPool = new ArrayList<>();
        for(String r : AbstractDungeon.bossRelicPool) {
            AbstractRelic tmp = RelicLibrary.getRelic(r);
            if(tmp instanceof SkillbookRelic) {
                skillbookPool.add((SkillbookRelic) tmp);
            }
        }
        for(int i = 0;i<SKILLBOOK_SPAWN_AMOUNT;i++) {
            if(skillbookPool.size()>0) {
                skillbookPool.remove(rng.random(skillbookPool.size()-1));
            }
        }
        if(!skillbookPool.isEmpty()) {
            if(AbstractDungeon.bossRelicPool.removeIf(relic -> RelicLibrary.getRelic(relic) instanceof SkillbookRelic && skillbookPool.contains(RelicLibrary.getRelic(relic)))) {
                skillbookPool.forEach(sb -> logger.info("Removed Skillbook: " + sb.name));
            }
        }
        /*for(Iterator<String> it = AbstractDungeon.bossRelicPool.iterator();it.hasNext(); ) {
            String r = it.next();
            AbstractRelic tmp = RelicLibrary.getRelic(r);
            if (tmp instanceof SkillbookRelic && skillbookPool.contains(tmp)) {
                AbstractDungeon.bossRelicPool.remove(tmp);
            }
        }*/
    }
    
    @Override
    public void receivePostPowerApplySubscriber(AbstractPower p, AbstractCreature target, AbstractCreature source) {
        for (AbstractRelic r : AbstractDungeon.player.relics) {
            if (r instanceof  AspirationRelic) {
                ((AspirationRelic)r).onApplyPower(p, target, source);
            }
        }
    }
}