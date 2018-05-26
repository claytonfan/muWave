// NAME
//   eco.c - ecosystem
//     Entry for music ecosystem development.
// NOTES
//   None.
//
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include "eco.h"
//
static T_WORLD world;
//
// *** Ecosystem Entry *********************************************************
//
int eco( T_PARAM *p )
{
  if( p->trace >= 1 ) Print( "Initiating Ecosystem. . .\n");
  //
  // Create world
  // Define environment, and
  // Create population of one or more species.
  //
  genesis( &world, p );
  //
  if( p->miditest ) return( midi_soundcheck( p ) );
  //
  if( p->trace >= 1 ) Print( "Starting Evolution. . .\n");
  //
  // Eons is meant to be used for continuos streaming
  //
  for( p->ieons = 0; p->ieons < p->neons && !end_of_eons(p); p->ieons++ ) {
	prepare_ecosystem( &world, p );
	for( p->igens = 0; p->igens < p->ngens; p->igens++ ) {   // generations
	  //
	  // Encode into genome.
	  // Compute fitness for all species of the current generation.
      //
	  if( encode( world.gen0, p ) < NORMAL ) break;
	  compute_fitness(&world, p );
	  if( fit_to_organize( p ) ) break;
	  //
	  // Epigenetic Interventions
	  //
	  adjust_ecosystem(&world, p);
	  //
	  // Evolve the population of species.
	  //
	  for( int i = 0; i < p->nspecies; i++ ) {
	    if( p->species[i].active ) {              // for each active species
		  T_SPECIE *sp0 = &(world.gen0[i]);       // current generation
		  T_SPECIE *sp1 = &(world.gen1[i]);       // new     generation
		  display_data( sp0, i, FALSE, p );
		  //
		  // Select parents by fitness to produce offspring of 2 children.
		  //  (There are several methods of parent selection reproduct)
		  // Reproduce by crossover conditioned on crossover rate.
		  // Mutate offspring conditioned on mutation rate.
		  //
		  for( int j=0; j < p->species[i].nlives; j+=2 ) {
		    T_PARENTS *parents = select_parents( i, sp0->lives, p );
		    reproduce(i, sp0->lives, sp1->lives, parents, j, p );
		    mutate(   i, sp1->lives, j, p );
		  }                                     // for every 2 child organisms
	    }                                       // if active species
	  }                                         // for all species
	  //
	  // Speciate by divergence or hybridization
	  //
	  if( diverge(&world, p) < NORMAL ) break;
	  //
	  // Swap new with old generation of organisms for all species.
	  //
	  swap_generations(p);
	}                              // for all generations
	if( p->status >= NORMAL ) {
	  //
	  // End of an eon of ecosystem evolution.
      //   - Final encode of genome
      //   - organize intra- and inter-species communities
	  //   - Get final fittest information.
	  //   - Express ecosystem into song -- community into musical events
	  // Note that after the swap, gen0 is the current generation.
	  //
      if( encode( world.gen0, p ) >= NORMAL ) {
		do {
		  organize( &world, p );
		} while( !fit_to_express(p) );
		compute_fitness(  &world, p);
		enhance_ecosystem(&world, p);
		display_final(p);
		if( express( &world, p ) >= NORMAL) {
		  display_summary(&world, p);
		}
	  }                           // if success in encode()
	}                             // if one eon of evolution is normal
  }                               // for all eons 
  apocalypse( &world, p );        // Destroy world -- population and environment
  return( p->status );
}                                                                       // eco()
