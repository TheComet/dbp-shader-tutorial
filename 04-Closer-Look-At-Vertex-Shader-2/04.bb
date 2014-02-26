[b][center]TheComet's Shader Tutorial[/center]
[center]04 - A Closer Look At The Vertex Shader - Part 2[/center][/b]

[b]Synopsis[/b]

[b]*[/b] What are normals and how can I have fun with them?



[b]What are normals?[/b]

You may have heard of these "normals" here and there. A [b]surface normal[/b] is a [b]unit vector[/b], that is, a vector with the length of exactly 1, perpendicular to the surface.

[img]surface normal.png[/img]

Since vertex shaders process vertices and not surfaces, each vertex is given a pre-calculated normal based on the surfaces it connects. This new normal is called the [b]vertex normal[/b], and can be accessed via the semantic [b]NORMAL0[/b].

[img]vertex normal.png[/img]

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
	position : POSITION0;
	normal : NORMAL0;
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

Now, in DBP, simply load and apply the shader to a more complex object:
[code]rem setup screen
sync on
sync rate 60
backdrop on
hide mouse

rem load character
load object "char.x", 1

rem load and apply fatness shader
load effect "fatness.fx", 1, 0
set object effect 1, 1

rem main loop
do

	rem change fatness factor with arrow keys
	set cursor 0, 0
	print "Use arrow keys to change fatness"
	if upkey() then inc fatness#, 0.01
	if downkey() then dec fatness#, 0.01
	set effect constant float 1, "fatness", fatness#

	rem simple camera control
	angle# = wrapvalue( angle# + mousemovex() )
	inc height#, mousemovey()
	set camera to follow 0, 0, 0, angle#, 20, height#, 4, 0
	point camera 0, 0, 0

	sync
loop[/code]

As you can see, manipulating vertices with shaders is extremely easy and fast.

If you don't understand how this works, let me give you some help. [b]input.position[/b] is the current location of the vertex. We also have access to a directional vector, the [b]vertex normal[/b], which tells us which direction is "away" from the object (perpendicular). If we multiply [b]fatness[/b] with [b]input.normal[/b], all we do is we change the length of the normal vector. By adding [b]input.position[/b] and [b]input.normal*fatness[/b] together, we're moving the vertex "away" from the object by exactly the distance [b]fatness[/b] specifies.



[b]Summary[/b]

[b]*[/b] A vertex normal is a directional vector, which is the average of all surface normals it connects. In layman's terms: "It poins away from the object's surface".
[b]*[/b] A vertex normal is a [b]unit vector[/b]: This means it has the lenght of 1.0



[b]Links[/b]

Proceed to the next tutorial here.

TheComet
