package nl.surfnet.mockoleth.model;


public class SpConfigurationImpl extends CommonConfigurationImpl implements SpConfiguration {

    public SpConfigurationImpl() {
        reset();
    }

    @Override
    public void reset() {
        super.entityId = "sp";
    }
}
