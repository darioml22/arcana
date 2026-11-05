public class ModCard extends Card {

    public ModCard(
            Properties properties,
            int spellId,
            Enum<Type> cardType,
            String cardName,
            String cardDescription,
            int textureIndex
    ) {
        super(properties, cardType, cardName, cardDescription, textureIndex);
    }

    public String execute() {
        return "Mod card executed: " + getCardName();
    }
}