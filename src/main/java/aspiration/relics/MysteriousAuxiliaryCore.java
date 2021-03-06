package aspiration.relics;

import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import aspiration.relics.abstracts.AspirationRelic;

public class MysteriousAuxiliaryCore extends AspirationRelic{
public static final String ID = "aspiration:MysteriousAuxiliaryCore";
	
	private static final int ORB_AMOUNT = 1;
	private static final int TURN_INTERVAL = 3;
	private static final int START_CHARGE = 0;
	
    public MysteriousAuxiliaryCore() {
        super(ID, "MysteriousAuxiliaryCore.png", RelicTier.RARE, LandingSound.CLINK);
    }
    
    @Override
	public void onEquip() {
        startingCharges();
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0] + ORB_AMOUNT + DESCRIPTIONS[1] + TURN_INTERVAL + DESCRIPTIONS[2];
    }
    
    @Override
    public void onPlayerEndTurn()
    {
    	if(((GameActionManager.turn - 1) % TURN_INTERVAL) == 0) {
    		AbstractDungeon.actionManager.addToBottom(new ChannelAction(AbstractOrb.getRandomOrb(true)));
    		startingCharges();
    	} else {
    		flash();
    		manipCharge(1);
    	}
    }
    
    private void startingCharges()
    {
        setCounter(START_CHARGE);
    }
    
    private void manipCharge(int amt) {
        if (counter < 0) {
            counter = 0;
        }
        setCounter(counter + amt);
    }
    
    public AbstractRelic makeCopy() {
        return new MysteriousAuxiliaryCore();
    }
}
