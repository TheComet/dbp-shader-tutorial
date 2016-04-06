// ----------------------------------------------------------------------------
// Projection matrices
// ----------------------------------------------------------------------------

float4x4 matWorldViewProjection : WORLDVIEWPROJECTION;

// ----------------------------------------------------------------------------
// Input and output structs
// ----------------------------------------------------------------------------

struct VS_INPUT
{
	float4 position : POSITION0;
};

struct VS_OUTPUT
{
	float4 position : POSITION0;
};

struct PS_INPUT
{
};

struct PS_OUTPUT
{
	float4 colour : COLOR;
};

// ----------------------------------------------------------------------------
// Vertex shader
// ----------------------------------------------------------------------------

VS_OUTPUT vs_main( VS_INPUT input )
{
	// declare output struct, so we can write output data
	VS_OUTPUT output;

	// set the w component to half the size
	input.position.w = 0.5f;

	// take each position attribute of the incoming vertex and transform it into projection space
	output.position = mul( input.position, matWorldViewProjection );

	// return output data
	return output;
}

// ----------------------------------------------------------------------------
// Pixel shader
// ----------------------------------------------------------------------------

PS_OUTPUT ps_main( PS_INPUT input )
{
	// declare output struct, so we can write output data
	PS_OUTPUT output;

	// this is a very simple shader. colour every pixel green
	output.colour = float4( 0.0f, 1.0f, 0.0f, 1.0f );

	// return output data
	return output;
}

// ----------------------------------------------------------------------------
// Techniques
// ----------------------------------------------------------------------------

technique Default
{
	pass p0
	{
		VertexShader = compile vs_1_1 vs_main();
		PixelShader = compile ps_1_1 ps_main();
	}
}