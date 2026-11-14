#!/usr/bin/env python3
"""Generate furniture models, recipes, translations and matching collision shapes."""
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
NS = 'civiccraft'
ASSET = RES / 'assets' / NS
DATA = RES / 'data' / NS
WOODS = {
    'oak': ('Oak', 'Дуб'), 'spruce': ('Spruce', 'Ель'), 'birch': ('Birch', 'Берёза'),
    'jungle': ('Jungle', 'Тропическое дерево'), 'acacia': ('Acacia', 'Акация'),
    'dark_oak': ('Dark Oak', 'Тёмный дуб'), 'mangrove': ('Mangrove', 'Мангр'),
    'cherry': ('Cherry', 'Вишня'), 'bamboo': ('Bamboo', 'Бамбук'),
    'crimson': ('Crimson', 'Багряное дерево'), 'warped': ('Warped', 'Искажённое дерево'),
    'pale_oak': ('Pale Oak', 'Бледный дуб'), 'poplar': ('Poplar', 'Тополь'),
}

def part(x, y, z, X, Y, Z, texture='wood'):
    return (x, y, z, X, Y, Z, texture)

def legs(x1, x2, z1, z2, height, width=2):
    return [part(x, 0, z, x+width, height, z+width, 'leg') for x in (x1, x2) for z in (z1, z2)]

# The north-facing model has its usable front toward -Z.
CHAIR = legs(3, 11, 3, 11, 7) + [part(2,7,2,14,9,14),
    part(3,9,12,5,16,14,'leg'), part(11,9,12,13,16,14,'leg'), part(5,11,12,11,15,14)]
SHELF = [part(0,0,3,16,2,16),part(0,2,3,2,16,16,'leg'),part(14,2,3,16,16,16,'leg'),
    part(2,2,14,14,15,16),part(2,7,3,14,9,14),part(0,14,3,16,16,16)]
KINDS = {
    'chair': ('Chair', 'Стул', 9, False, 0, CHAIR, ['P  ','PPP','S S']),
    'armchair': ('Armchair', 'Кресло', 9, False, 0, CHAIR + [
        part(2,9,4,4,12,6,'leg'),part(12,9,4,14,12,6,'leg'),
        part(1,12,3,5,14,14),part(11,12,3,15,14,14),part(4,8,4,12,10,11,'cloth')],
        ['P P','PWP','S S']),
    'stool': ('Stool', 'Табурет', 8, False, 0,
        legs(4,10,4,10,6) + [part(3,6,3,13,8,13)], ['PPP',' S ','S S']),
    'table': ('Table', 'Стол', 0, False, 0, legs(1,13,1,13,13) + [
        part(0,13,0,16,16,16),part(1,11,2,15,13,3,'leg'),part(1,11,13,15,13,14,'leg')],
        ['PPP','S S','S S']),
    'cabinet': ('Cabinet', 'Шкаф', 0, True, 0, [
        part(0,2,2,16,15,16),part(0,15,1,16,16,16),part(1,3,1,7.75,14,2,'leg'),
        part(8.25,3,1,15,14,2,'leg'),part(6,7,0.5,7,9,1,'metal'),part(9,7,0.5,10,9,1,'metal')]
        + legs(1,13,3,13,2), ['PPP','PCP','PPP']),
    'bookcase': ('Bookcase', 'Книжный шкаф', 0, True, 6, SHELF + [
        part(7.5,2,11,8.5,14,14,'leg')], ['PPP','PBP','PPP']),
    'shop_shelf': ('Shop Shelf', 'Магазинная полка', 0, True, 6, SHELF,
        ['PSP','PPP','PSP']),
    'counter': ('Shop Counter', 'Прилавок', 0, True, 0, [
        part(0,0,2,16,2,16,'leg'),part(0,2,3,16,13,15),part(1,3,2,15,12,3,'leg'),
        part(6,7,1,10,8,2,'metal'),part(0,13,0,16,16,16)], ['PPP','PCP','S S']),
    'bench': ('Park Bench', 'Парковая лавочка', 8, False, 0, [
        part(1,0,4,3,6,12,'leg'),part(13,0,4,15,6,12,'leg'),
        part(0,6,3,16,8,6),part(0,6,6.25,16,8,9),part(0,6,9.25,16,8,12),
        part(1,8,12,3,15,14,'leg'),part(13,8,12,15,15,14,'leg'),
        part(0,10,12,16,12,13),part(0,13,12,16,15,13)], ['PPP','SSS','P P']),
    'pedestal': ('Display Pedestal', 'Пьедестал для экспоната', 0, True, 1, [
        part(2,0,2,14,2,14,'leg'),part(4,2,4,12,13,12),part(1,13,1,15,15.2,15),
        part(3,15.2,3,13,16,13,'cloth')], ['PWP',' C ','PPP']),
    'sofa': ('Sofa', 'Диван', 8, False, 0, legs(1,13,3,12,1) + [
        part(0,1,2,16,5,15),part(2,5,2,14,8,12,'cloth'),part(2,5,12,14,14,15,'cloth'),
        part(0,5,2,2,11,15),part(14,5,2,16,11,15)], ['WPW','WWW','PPP']),
    'bar_stool': ('Bar Stool', 'Барный табурет', 12, False, 0,
        legs(4,10,4,10,10) + [part(3,10,3,13,12,13),part(4,3,4,12,4,6,'metal'),
        part(4,3,10,12,4,12,'metal')], [' W ','PPP','SIS']),
    'waiting_seat': ('Waiting Room Seat', 'Сиденье для зала ожидания', 8, False, 0, [
        part(1,0,4,3,6,12,'metal'),part(13,0,4,15,6,12,'metal'),part(1,5,6,15,6,10,'metal'),
        part(0,6,3,16,8,12),part(0,8,12,16,14,14)], ['PPP','IPI',' I ']),
    'coffee_table': ('Coffee Table', 'Журнальный столик', 0, False, 0,
        legs(2,12,4,10,6) + [part(0,6,2,16,8,14),part(2,2,4,14,3,12)], ['PPP',' P ','S S']),
    'side_table': ('Side Table', 'Приставной столик', 0, False, 0, [
        part(3,0,3,13,2,13),part(7,2,7,9,11,9,'leg'),part(3,11,3,13,13,13)],
        [' PP',' S ','PP ']),
    'writing_desk': ('Writing Desk', 'Письменный стол', 0, True, 0, [
        part(0,13,1,16,16,15),part(0,0,2,2,13,14,'leg'),part(11,0,2,16,13,14),
        part(2,9,12,11,13,14),part(11.2,7,1,15.8,12.5,2),part(11.2,1,1,15.8,6.5,2),
        part(12.4,9,0.5,14.6,10,1,'metal'),part(12.4,3,0.5,14.6,4,1,'metal')],
        ['PPP','SPC','S P']),
    'cafe_table': ('Café Table', 'Столик для кафе', 0, False, 0, [
        part(4,0,4,12,2,12,'metal'),part(7,2,7,9,12,9,'metal'),
        part(3,12,1,13,14,15),part(1,12,3,3,14,13),part(13,12,3,15,14,13)],
        ['PPP',' I ',' I ']),
    'dresser': ('Dresser', 'Комод', 0, True, 0, [
        part(1,1,2,15,14,15),part(0,14,1,16,16,16),
        part(2,2,1.1,14,5.7,2),part(2,6.1,1.1,14,9.7,2),part(2,10.1,1.1,14,13.7,2),
        part(6,3.5,0.5,10,4.5,1.1,'metal'),part(6,7.5,0.5,10,8.5,1.1,'metal'),
        part(6,11.5,0.5,10,12.5,1.1,'metal')], ['PCP','PPP','PPP']),
    'kitchen_unit': ('Kitchen Unit', 'Кухонная тумба', 0, True, 0, [
        part(1,0,3,15,2,15,'leg'),part(0,2,2,16,14,16),part(0,14,0,16,16,16,'stone'),
        part(1,3,1,7.75,13,2),part(8.25,3,1,15,13,2),
        part(5.5,10,0.5,7,11,1,'metal'),part(9,10,0.5,10.5,11,1,'metal')],
        ['TTT','PCP','PPP']),
    'filing_cabinet': ('Filing Cabinet', 'Картотечный шкаф', 0, True, 0, [
        part(3,0,2,13,16,15),part(4,1,1,12,7.6,2),part(4,8.1,1,12,15,2),
        part(6.2,4,0.5,9.8,5,1,'metal'),part(6.2,11.2,0.5,9.8,12.2,1,'metal'),
        part(6.6,5.15,0.9,9.4,6.35,1.1,'paper'),part(6.6,12.35,0.9,9.4,13.55,1.1,'paper')],
        ['PIP','PCP','PIP']),
    'wall_shelf': ('Wall Shelf', 'Настенная полка', 0, True, 3, [
        part(0,7,5,16,9,16),part(0,8.5,14.5,16,11,16),part(2,4,12,4,7,16,'metal'),
        part(12,4,12,14,7,16,'metal')], ['PPP','ICI','   ']),
    'market_stand': ('Market Stand', 'Рыночный стенд', 0, True, 3, [
        part(0,0,3,16,2,16),part(0,2,7,16,6,16),part(0,6,2,16,8,16),
        part(0,8,14,2,14,16,'leg'),part(14,8,14,16,14,16,'leg'),
        part(0,14,4,16,16,16,'cloth'),part(0,13,3,16,14,5,'cloth')], ['WWW','SCS','PPP']),
    'display_case': ('Glass Display Case', 'Стеклянная витрина', 0, True, 1, [
        part(1,0,1,15,3,15),part(2,3,2,14,4,14,'cloth'),
        part(1,13.5,1,15,15,2),part(1,13.5,14,15,15,15),part(1,13.5,2,2,15,14),
        part(14,13.5,2,15,15,14),part(2,14,2,14,14.25,14,'glass'),
        part(1,3,1,2,13.5,2),part(14,3,1,15,13.5,2),part(1,3,14,2,13.5,15),
        part(14,3,14,15,13.5,15),part(2,3,1.25,14,13.5,1.5,'glass'),
        part(2,3,14.5,14,13.5,14.75,'glass'),part(1.25,3,2,1.5,13.5,14,'glass'),
        part(14.5,3,2,14.75,13.5,14,'glass')], ['GGG','GCG','PPP']),
    'book_stand': ('Book Stand', 'Подставка для книги', 0, True, 1, [
        part(3,0,3,13,2,13),part(6,2,6,10,11,10,'leg'),part(2,10,4,14,12,13),
        part(2,12,9,14,13,13),part(2,13,11,14,14,13)], [' BP',' C ','PSP']),
    'display_rack': ('Display Rack', 'Выставочный стеллаж', 0, True, 6, [
        part(0,0,2,2,16,14,'leg'),part(14,0,2,16,16,14,'leg'),part(2,1,2,14,3,14),
        part(2,8,2,14,10,14),part(2,13,12,14,15,14,'metal'),part(2,4,12,14,6,14,'metal')],
        ['IPI','PCP','IPI']),
    'storage_crate': ('Storage Crate', 'Ящик для хранения', 0, True, 0, [
        part(1,0,1,15,2,15),part(1,2,1,15,10,15),part(1,10,1,15,12,15),
        part(.5,2,.5,3,12,3,'leg'),part(13,2,.5,15.5,12,3,'leg'),
        part(.5,2,13,3,12,15.5,'leg'),part(13,2,13,15.5,12,15.5,'leg'),
        part(5,6,.5,11,8,1,'metal')], ['PSP','PCP','PPP']),
}
POST = [part(4,0,4,12,2,12,'metal'),part(6,2,6,10,3,10,'metal'),
        part(7,3,7,9,13,9,'metal'),part(6,12,6,10,14,10,'metal'),part(7,14,7,9,15,9,'metal')]
ROPE = [
    part(7.25,11.75,6,8.75,13.25,8,'rope'),
    part(7.25,11.1,4,8.75,12.6,6.1,'rope'),
    part(7.25,10.65,2,8.75,12.15,4.1,'rope'),
    part(7.25,10.5,0,8.75,12,2.1,'rope'),
]

def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2)+'\n')

def cuboid(box):
    x,y,z,X,Y,Z,tex=box
    return {'from':[x,y,z], 'to':[X,Y,Z],
            'faces': {face:{'texture':'#'+tex} for face in ('north','east','south','west','up','down')}}

def model(boxes, textures, parent='minecraft:block/block'):
    return {'parent':parent,'ambientocclusion':False, 'textures':textures,'elements':[cuboid(b) for b in boxes]}

def finish(name, en, ru, recipe, count=1, tool='axe', unlock='minecraft:oak_planks'):
    rid=f'{NS}:{name}'
    write(ASSET/'items'/f'{name}.json', {'model':{'type':'minecraft:model','model':f'{NS}:block/{name}'}})
    write(DATA/'recipe'/f'{name}.json', {**recipe,'result':{'id':rid,'count':count}})
    write(DATA/'advancement'/'recipes'/f'{name}.json', {
        'parent':'minecraft:recipes/root',
        'criteria':{'materials':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':unlock}]}}},
        'requirements':[['materials']], 'rewards':{'recipes':[rid]}})
    write(DATA/'loot_table'/'blocks'/f'{name}.json', {'type':'minecraft:block','pools':[{
        'rolls':1,'entries':[{'type':'minecraft:item','name':rid}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
    langs[0][f'block.{NS}.{name}']=en
    langs[1][f'block.{NS}.{name}']=ru
    tags[tool].append(rid)

langs=[{'itemGroup.civiccraft':'CivicCraft'}, {'itemGroup.civiccraft':'CivicCraft — мебель'}]
tags={'axe':[], 'pickaxe':[]}
for wood,(en_wood,ru_wood) in WOODS.items():
    for kind,(en,ru,seat,storage,display,boxes,pattern) in KINDS.items():
        name=f'{wood}_{kind}'
        tv=f'minecraft:block/{wood}_planks'
        textures={'wood':tv, 'leg':tv, 'cloth':'minecraft:block/red_wool',
                  'metal':'minecraft:block/iron_block', 'stone':'minecraft:block/smooth_stone',
                  'glass':'minecraft:block/glass', 'paper':'minecraft:block/white_wool','particle':tv}
        write(ASSET/'models'/'block'/f'{name}.json', model(boxes,textures))
        write(ASSET/'blockstates'/f'{name}.json', {'variants':{
            f'facing={direction}':{'model':f'{NS}:block/{name}', 'y':turn}
            for direction,turn in zip(('north','east','south','west'),(0,90,180,270))}})
        options={'P':f'minecraft:{wood}_planks', 'S':'minecraft:stick', 'C':'minecraft:chest',
                 'B':'minecraft:book', 'W':'minecraft:red_wool', 'I':'minecraft:iron_nugget',
                 'T':'minecraft:smooth_stone', 'G':'minecraft:glass'}
        key={c:options[c] for c in sorted(set(''.join(pattern))) if c!=' '}
        finish(name,f'{en_wood} {en}',f'{ru} ({ru_wood})',
               {'type':'minecraft:crafting_shaped','category':'misc','pattern':pattern,'key':key},unlock=f'minecraft:{wood}_planks')
for color,en,ru in [('red','Red','красный'),('blue','Blue','синий'),('black','Black','чёрный'),('white','White','белый')]:
    name=f'{color}_museum_stanchion'
    tex={'metal':'minecraft:block/gold_block','rope':f'minecraft:block/{color}_wool','particle':'minecraft:block/gold_block'}
    write(ASSET/'models'/'block'/f'{name}.json',model(POST,tex))
    write(ASSET/'models'/'block'/f'{name}_rope.json',model(ROPE,tex))
    write(ASSET/'blockstates'/f'{name}.json',{'multipart':[{'apply':{'model':f'{NS}:block/{name}'}}]+[
        {'when':{d:'true'},'apply':{'model':f'{NS}:block/{name}_rope','y':a}}
        for d,a in zip(('north','east','south','west'),(0,90,180,270))]})
    finish(name,f'{en} Museum Stanchion',f'Музейная стойка ({ru} канат)',{
        'type':'minecraft:crafting_shaped','category':'misc','pattern':[' N ',' S ','IGI'],
        'key':{'N':f'minecraft:{color}_wool','S':'minecraft:stick','I':'minecraft:iron_nugget','G':'minecraft:gold_nugget'}},
        2,'pickaxe','minecraft:gold_nugget')
for f,l in zip(('en_us','ru_ru'),langs):write(ASSET/'lang'/f'{f}.json',l)
for tool,ids in tags.items():
    write(RES/'data'/'minecraft'/'tags'/'block'/'mineable'/f'{tool}.json',{'replace':False,'values':ids})

def java_coords(boxes):
    return ',\n            '.join('{' + ', '.join(str(v) for v in p[:6]) + '}' for p in boxes)
enum_items=[]
for key,(_,_,seat,storage,display,boxes,_) in KINDS.items():
    enum_items.append(f'    {key.upper()}("{key}", {seat}, {str(storage).lower()}, {display}, new double[][] {{\n            {java_coords(boxes)}\n    }})')
java='''package studio.detemkur.civiccraft;

import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// Generated by tools/generate_resources.py from the furniture model geometry.
public enum FurnitureKind {
'''+',\n'.join(enum_items)+''';

    public final String path;
    public final int seatHeight;
    public final boolean storage;
    public final int displaySlots;
    public final Map<Direction, VoxelShape> shapes;

    FurnitureKind(String path, int seatHeight, boolean storage, int displaySlots, double[][] boxes) {
        this.path = path;
        this.seatHeight = seatHeight;
        this.storage = storage;
        this.displaySlots = displaySlots;
        VoxelShape shape = Shapes.empty();
        for (double[] box : boxes) {
            shape = Shapes.or(shape, Block.box(box[0], box[1], box[2], box[3], box[4], box[5]));
        }
        this.shapes = Shapes.rotateHorizontal(shape.optimize());
    }
}
'''
(ROOT/'src/main/java/studio/detemkur/civiccraft/FurnitureKind.java').write_text(java)
print(f'{len(WOODS)*len(KINDS)+4} furniture blocks with recipes, models and loot tables')
