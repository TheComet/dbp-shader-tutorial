[b][center]TheComet's Shader Tutorial[/center]
[center]04 - Vertex Normals[/center][/b]

[b]Synopsis[/b]

[b]*[/b] What normals are.
[b]*[/b] How to have fun with normals.



[b]What are normals?[/b]

You may have heard of these "normals" here and there. A [b]surface normal[/b] is a [b]unit vector[/b], that is, a vector with the length of exactly 1, perpendicular to the surface.

[img]http://i254.photobucket.com/albums/hh100/TheComet92/shader-tutorial-res/Surface_normal_illustration_zpsdfb10361.png[/img]

Since vertex shaders process vertices and not surfaces, each vertex is given a pre-calculated normal based on the average of the surface normals surrounding it. This new normal is called the [b]vertex normal[/b], and can be accessed via the semantic [b]NORMAL0[/b].

[img]http://i254.photobucket.com/albums/hh100/TheComet92/shader-tutorial-res/fig03_zps9971f11e.gif[/img]

Normals allow you to do some cool lighting effects, but that's something for a later tutorial. For now, just know that they exist.



[b]How can I have fun with normals?[/b]

One cool thing you can do with vertex normals is use them to evenly change the surface area of an object. That is, you can make your character fatter/thinner.

Try changing your vertex shader to the following.

Add this to the very top of your shader, underneath where the projection matrices are declared:
[code]// determines how fat the character is
float fatness = 0.0f;[/code]

We can gain access to the object's normals through the [b]NORMAL0[/b] semantic by adding this to the vertex shader input struct:
[code]struct VS_INPUT
{
	float4 position : POSITION0;
	float3 normal : NORMAL0;
};[/code]

And now change your vertex shader to the following:
[code]VS_OUTPUT vs_main( VS_INPUT input )
{
	// declare output struct, so we can write output data
	VS_OUTPUT output;

	// change fatness
	float4 fatPosition = input.position + (input.normal * fatness);

	// transform new fat position into projection space
	output.position = mul( fatPosition, matWorldViewProjection );

	// return output data
	return output;
}[/code]

Then, in DBP, simply load and apply the shader to a more complex object:
[code]rem setup screen
sync on
sync rate 60
backdrop on
color backdrop 0
hide mouse

rem load character
load object "character.x", 1
xrotate object 1, 270

rem load shader
load effect "fatness.fx", 1, 0
set object effect 1, 1

rem catch shader compilation errors
perform checklist for effect errors
if checklist quantity() > 0
   do
      set cursor 0, 0
      print "shader errors have occurred!"
      for n = 1 to checklist quantity()
         print checklist string$(n)
      next n
      sync
   loop
endif

rem main loop
dist# = 50
do
	
	rem change fatness factor with arrow keys
	set cursor 0, 0
	print "Use arrow keys to change fatness"
	if upkey() then inc fatness#, 0.01
	if downkey() then dec fatness#, 0.01
	set effect constant float 1, "fatness", fatness#

   rem control camera
   angley# = wrapvalue(angley# + mousemovex()*0.5)
   anglex# = wrapvalue(anglex# - mousemovey()*0.5)
   inc dist#, mousemovez()*0.2
   if anglex# > 180 and anglex# < 270 then anglex# = 270
   if anglex# < 180 and anglex# > 90 then anglex# = 90
   position camera 0, 0, 0
   rotate camera anglex#, angley#, 0
   move camera dist#
   point camera 0, 0, 0

   rem refresh screen
   sync

rem end of main loop
loop[/code]

The results can be quite funny. The followingn shows the same model with different fatness factors:

[img]http://i254.photobucket.com/albums/hh100/TheComet92/shader-tutorial-res/fatness_zps8b1a6c77.png[/img]

As you can see, manipulating vertices with shaders is extremely easy and fast.

If you don't understand how this works, let me give you some help. [b]input.position[/b] is the location of the current vertex. We also have access to a directional vector, the [b]vertex normal[/b], which tells us which direction is "away" from the object (perpendicular). If we multiply [b]fatness[/b] with [b]input.normal[/b], all we do is we change the length of the normal vector. By adding [b]input.position[/b] and [b]input.normal*fatness[/b] together, we're moving the vertex "away" from the object by exactly the distance [b]fatness[/b] specifies.



[b]Summary[/b]

[b]*[/b] A vertex normal is a directional unit vector, which is the average of all surface normals it connects. In layman's terms: "It poins away from the object's surface".
[b]*[/b] A vertex normal is a [b]unit vector[/b]: This means it has the length of 1.0.



[b]Links[/b]

Proceed to the next tutorial: [href=]05 - UV Coordinates[/href]
Proceed to the previous tutorial here: [href=]03 - Vertex Shader Coordinate System[/href]

TheComet
