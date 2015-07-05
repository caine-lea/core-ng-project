package app.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author neo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SKUIndex {
    @XmlElement(name = "sku")
    public String sku;
    @XmlElement(name = "productId")
    public Integer productId;
    @XmlElement(name = "name")
    public String name;
    @XmlElement(name = "desc")
    public String description;
    @XmlElement(name = "vendorSKU")
    public String vendorSKU;
    @XmlElement(name = "price")
    public Double price;
    @XmlElement(name = "color")
    public String color;
    @XmlElement(name = "size")
    public String size;
}
