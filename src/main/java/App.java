import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/*
 * This Java source file was generated by the Gradle 'init' task.
 */
public class App {
    private ItemRepository itemRepository;
    private SalesPromotionRepository salesPromotionRepository;

    public App(ItemRepository itemRepository, SalesPromotionRepository salesPromotionRepository) {
        this.itemRepository = itemRepository;
        this.salesPromotionRepository = salesPromotionRepository;
    }

    public String bestCharge(List<String> inputs) {
        List<Item> all = itemRepository.findAll();
        Map<String, Item> itemMap = all.stream().collect(Collectors.toMap(Item::getId, item -> item));
        //TODO: write code here
        //1。先对inputs数据进行对象构建
        List<Item> inputItem = inputs.stream().map(str -> {
            String[] s = str.split(" ");
            Item item = itemMap.get(s[0]);
            return item;
        }).collect(Collectors.toList());

        List<Integer> numList = inputs.stream().map(str -> {
            String[] s = str.split(" ");
            return Integer.valueOf(s[2]);
        }).collect(Collectors.toList());

        //2。计算原价
        BigDecimal originPrice = useOriginPrice(inputItem, numList);

        //3。计算使用半价的价格
        HashMap<String, Object> halfPriceMap = useHalfPrice(inputItem, numList);
        BigDecimal halfPrice = (BigDecimal) halfPriceMap.get("resultPrice");
        List<Item> itemHaveHalf = (List<Item>) halfPriceMap.get("halfItem");

        //4。计算使用30-6的价格
        BigDecimal saveSixPrice = useSaveSixPrice(inputItem, numList);


        StringBuilder sb = new StringBuilder();
        sb.append("============= Order details =============\n");
        for(int i = 0;i<inputItem.size();i++) {
            Item item = inputItem.get(i);
            Integer num = numList.get(i);
            sb.append(item.getName()+" x "+num+" = "+ (int)item.getPrice()*num +" yuan\n");
        }
        sb.append("-----------------------------------\n");

        if(halfPrice.compareTo(saveSixPrice)>=0&&originPrice.compareTo(saveSixPrice)>0){
            //使用减6块钱
            sb.append("Promotion used:\n");
            sb.append("满30减6 yuan，saving 6 yuan\n");
            sb.append("-----------------------------------\n");
            sb.append("Total："+saveSixPrice.toBigInteger().intValue()+" yuan\n");
            sb.append("===================================");
        }else if(halfPrice.compareTo(originPrice)<0){
            //使用半价
            sb.append("Promotion used:\n");
            sb.append("Half price for certain dishes (");
            itemHaveHalf.forEach(item -> {
                sb.append(item.getName());
                sb.append("，");
            });
            sb.deleteCharAt(sb.length()-1);
            sb.append(")，saving ");
            sb.append(originPrice.subtract(halfPrice).toBigInteger().intValue());
            sb.append(" yuan\n");
            sb.append("-----------------------------------\n");
            sb.append("Total："+halfPrice.toBigInteger().intValue()+" yuan\n");
            sb.append("===================================");
        }else {
            //使用原价
            sb.append("Total："+originPrice.toBigInteger().intValue()+" yuan\n" );
            sb.append("===================================");
        }
        return sb.toString();
    }

    public HashMap<String,Object> useHalfPrice(List<Item> items,List<Integer> nums){
        BigDecimal result = new BigDecimal(0);
        SalesPromotion salesPromotion = salesPromotionRepository.findAll().get(1);
        List<String> relatedItems = salesPromotion.getRelatedItems();
        List<Item> itemHaveHalf = new ArrayList<>();
        HashSet set = new HashSet(relatedItems);
        for(int i = 0;i<items.size();i++){
            Item item = items.get(i);
            Integer num = nums.get(i);
            if(set.contains(item.getId())){
                //若半价列表有该商品 则价格乘上0.5
                result = result.add(BigDecimal.valueOf(item.getPrice()*num*0.5));
                itemHaveHalf.add(item);
            }else {
                result = result.add(BigDecimal.valueOf(item.getPrice()*num));
            }
        }
        HashMap<String,Object> map = new HashMap<>();
        map.put("resultPrice",result);
        map.put("halfItem",itemHaveHalf);
        return map;
    }
    public BigDecimal useOriginPrice(List<Item> items,List<Integer> nums){
        BigDecimal result = new BigDecimal(0);
        for(int i = 0;i<items.size();i++){
            Item item = items.get(i);
            Integer num = nums.get(i);
            result = result.add(BigDecimal.valueOf(item.getPrice() * num));
        }
        return result;
    }
    public BigDecimal useSaveSixPrice(List<Item> items,List<Integer> nums){
        BigDecimal result = new BigDecimal(0);
        for(int i = 0;i<items.size();i++){
            Item item = items.get(i);
            Integer num = nums.get(i);
            result = result.add(BigDecimal.valueOf(item.getPrice()*num));
        }
        if(result.compareTo(BigDecimal.valueOf(30))>=0){
            result = result.subtract(BigDecimal.valueOf(6));
        }
        return result;
    }
}
